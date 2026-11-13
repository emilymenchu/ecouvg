package gt.edu.uvg.ecouvg.servicio;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import gt.edu.uvg.ecouvg.modelo.Evidencia;
import gt.edu.uvg.ecouvg.modelo.ProgresoReto;
import gt.edu.uvg.ecouvg.modelo.RevisionEvidencia;
import gt.edu.uvg.ecouvg.modelo.Usuario;
import gt.edu.uvg.ecouvg.repositorio.RepositorioEvidencia;
import gt.edu.uvg.ecouvg.repositorio.RepositorioRevisionEvidencia;
import gt.edu.uvg.ecouvg.repositorio.RepositorioUsuario;

@Service
public class ServicioRevisionEvidencia {

    private static final Logger logger =
            LoggerFactory.getLogger(ServicioRevisionEvidencia.class);

    private final RepositorioEvidencia repositorioEvidencia;
    private final RepositorioRevisionEvidencia repositorioRevisionEvidencia;
    private final RepositorioUsuario repositorioUsuario;
    private final ServicioReto servicioReto;
    private final ServicioStorage servicioStorage;

    public ServicioRevisionEvidencia(
            RepositorioEvidencia repositorioEvidencia,
            RepositorioRevisionEvidencia repositorioRevisionEvidencia,
            RepositorioUsuario repositorioUsuario,
            ServicioReto servicioReto,
            ServicioStorage servicioStorage) {

        this.repositorioEvidencia = repositorioEvidencia;
        this.repositorioRevisionEvidencia = repositorioRevisionEvidencia;
        this.repositorioUsuario = repositorioUsuario;
        this.servicioReto = servicioReto;
        this.servicioStorage = servicioStorage;
    }

    @Transactional
    public RevisionEvidencia registrarRevision(
            Long idEvidencia,
            Long idRevisor,
            boolean aprobada) {

        Evidencia evidencia = repositorioEvidencia
                .buscarParaRevision(idEvidencia)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Evidencia no encontrada"
                        ));

        Usuario revisor = repositorioUsuario
                .findById(idRevisor)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Usuario no encontrado"
                        ));

        if (!evidencia.estaPendiente()) {
            throw new IllegalStateException(
                    "La evidencia ya fue revisada y cerrada"
            );
        }

        Usuario autor = evidencia.getUsuario();

        if (autor.getId().equals(revisor.getId())) {
            throw new IllegalArgumentException(
                    "No puedes revisar evidencia propia"
            );
        }

        ProgresoReto progresoReto = evidencia.getProgresoReto();

        if (progresoReto == null) {
            throw new IllegalStateException(
                    "La evidencia no tiene un progreso de reto asociado"
            );
        }

        boolean yaVoto = repositorioRevisionEvidencia
                .existsByEvidenciaIdAndRevisorId(
                        idEvidencia,
                        idRevisor
                );

        if (yaVoto) {
            throw new IllegalStateException(
                    "Ya revisaste esta evidencia"
            );
        }

        long totalRevisiones =
                repositorioRevisionEvidencia
                        .countByEvidenciaIdAndAprobada(idEvidencia, true)
                + repositorioRevisionEvidencia
                        .countByEvidenciaIdAndAprobada(idEvidencia, false);

        if (totalRevisiones >= 3) {
            throw new IllegalStateException(
                    "La evidencia solo acepta 3 revisiones"
            );
        }

        RevisionEvidencia revision =
                new RevisionEvidencia(evidencia, revisor, aprobada);

        RevisionEvidencia revisionGuardada =
                repositorioRevisionEvidencia.saveAndFlush(revision);

        long aprobaciones = repositorioRevisionEvidencia
                .countByEvidenciaIdAndAprobada(idEvidencia, true);

        long rechazos = repositorioRevisionEvidencia
                .countByEvidenciaIdAndAprobada(idEvidencia, false);

        if (aprobaciones >= 2) {
            evidencia.aprobar();
            repositorioEvidencia.saveAndFlush(evidencia);

            servicioReto.registrarEvidenciaAprobada(progresoReto);

            LocalDate hoy = LocalDate.now(
                    ZoneId.of("America/Guatemala")
            );

            autor.registroActividadValida(hoy);

            recompensarRevisor(idEvidencia);
            eliminarFotografias(evidencia.getRutaFotografia());

        } else if (rechazos >= 2) {
            evidencia.rechazar();
            repositorioEvidencia.save(evidencia);

            recompensarRevisor(idEvidencia);
            eliminarFotografias(evidencia.getRutaFotografia());
        }

        return revisionGuardada;
    }

    private void recompensarRevisor(Long idEvidencia) {
        List<RevisionEvidencia> revisiones =
                repositorioRevisionEvidencia
                        .findByEvidenciaId(idEvidencia);

        for (RevisionEvidencia revisionActual : revisiones) {
            if (!revisionActual.estaRecompensada()) {
                Usuario revisor = revisionActual.getRevisor();

                revisor.sumarPuntos(2);
                revisionActual.marcarRecompensada();

                repositorioUsuario.save(revisor);
                repositorioRevisionEvidencia.save(revisionActual);
            }
        }
    }

    private void eliminarFotografias(String rutaFotografia) {
        if (rutaFotografia == null || rutaFotografia.isBlank()) {
            return;
        }

        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            throw new IllegalStateException(
                    "Para eliminar la fotografía debe existir una transacción"
            );
        }

        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        try {
                            servicioStorage.eliminarImagen(
                                    "evidencias",
                                    rutaFotografia
                            );

                            logger.info(
                                    "Fotografía {} eliminada",
                                    rutaFotografia
                            );
                        } catch (Exception e) {
                            logger.error(
                                    "No se pudo eliminar la fotografía {}",
                                    rutaFotografia,
                                    e
                            );
                        }
                    }
                }
        );
    }
}