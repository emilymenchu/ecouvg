package gt.edu.uvg.ecouvg.repositorio;

import gt.edu.uvg.ecouvg.modelo.CategoriaGuia;
import gt.edu.uvg.ecouvg.servicio.ServicioGuia;
import jakarta.persistence.EntityManager;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.*;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactory;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.persistenceunit.PersistenceManagedTypes;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

/** Persistencia real en H2, aislada de Supabase y del resto de entidades. */
class RepositorioGuiaTest {
    private LocalContainerEntityManagerFactoryBean factory;
    private EntityManager em;
    private ServicioGuia servicio;

    @BeforeEach void preparar() {
        var ds = new JdbcDataSource();
        ds.setURL("jdbc:h2:mem:guia;MODE=PostgreSQL;DB_CLOSE_DELAY=-1");
        factory = new LocalContainerEntityManagerFactoryBean();
        factory.setDataSource(ds);
        factory.setManagedTypes(PersistenceManagedTypes.of(CategoriaGuia.class.getName()));
        factory.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
        factory.setJpaPropertyMap(Map.of("hibernate.hbm2ddl.auto", "create-drop"));
        factory.afterPropertiesSet();
        em = factory.getObject().createEntityManager();
        servicio = new ServicioGuia(new JpaRepositoryFactory(em).getRepository(RepositorioGuia.class));
    }

    @AfterEach void cerrar() {
        if (em != null) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            em.close();
        }
        if (factory != null) factory.destroy();
    }

    @Test void cargaEjemplosOrdenadosYReflejaEdicionSinReiniciar() {
        em.getTransaction().begin();
        em.persist(new CategoriaGuia("nuevo", "Categoría nueva", "verde", "!",
            "Descripción desde BD", List.of("Café", "Hojas"), "Preparación",
            "https://example.org", 2));
        em.getTransaction().commit();
        em.clear();
        var datos = servicio.buscar("CAFE");
        assertEquals(1, datos.size());
        assertEquals(List.of("Café", "Hojas"), datos.getFirst().ejemplos());
        em.clear();
        assertEquals("Categoría nueva", datos.getFirst().nombre());
        // Simula edición desde SQL Editor, fuera del contexto de persistencia.
        em.getTransaction().begin();
        em.createNativeQuery("UPDATE guia_contenedores SET nombre = 'Nombre editado' WHERE slug = 'nuevo'").executeUpdate();
        em.getTransaction().commit();
        em.clear();
        assertEquals("Nombre editado", servicio.obtener("nuevo").orElseThrow().nombre());
        assertTrue(servicio.obtener("ausente").isEmpty());
    }

    @Test void ordenPersistidoYTablaVacia() {
        assertTrue(servicio.buscar(null).isEmpty());
        em.getTransaction().begin();
        for (int orden : List.of(2, 0, 1)) {
            em.persist(new CategoriaGuia("cat" + orden, "Categoría " + orden,
                "gris", "!", "Descripción", List.of(), "Preparación", "https://example.org", orden));
        }
        em.getTransaction().commit();
        em.clear();
        assertEquals(List.of("cat0", "cat1", "cat2"),
            servicio.buscar("").stream().map(c -> c.slug()).toList());
    }
}
