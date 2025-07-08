package org.example.Dao;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;
import org.example.model.Membre;
import org.example.util.JpaUtil;

import java.util.List;

public class MembreDao {
    public static Membre findOrCreate(String pseudo) {
        EntityManager em = JpaUtil.getEntityManager();
        Membre membre;

        try {
            em.getTransaction().begin();

            // Rechercher un membre par pseudo
            TypedQuery<Membre> query = em.createQuery(
                    "SELECT m FROM Membre m WHERE m.pseudo = :pseudo", Membre.class);
            query.setParameter("pseudo", pseudo);

            membre = query.getSingleResult();
            em.getTransaction().commit();

        } catch (NoResultException e) {
            // Si non trouvé → créer
            membre = new Membre();
            membre.setPseudo(pseudo);
            membre.setBanned(false);

            em.persist(membre);
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }

        return membre;
    }

    public void save(Membre membre, String message) {
        EntityManager em = JpaUtil.getEntityManager();
        em.getTransaction().begin();
        em.persist(membre);
        em.getTransaction().commit();
        em.close();
    }

    public Membre findByPseudo(String pseudo) {
        EntityManager em = JpaUtil.getEntityManager();
        Membre result = null;
        try {
            result = em.createQuery("SELECT m FROM Membre m WHERE m.pseudo = :p", Membre.class)
                    .setParameter("p", pseudo)
                    .getSingleResult();
        } catch (Exception e) {
            // Aucun résultat trouvé
        }
        em.close();
        return result;
    }

    public List<Membre> findAll() {
        EntityManager em = JpaUtil.getEntityManager();
        List<Membre> membres = em.createQuery("SELECT m FROM Membre m", Membre.class).getResultList();
        em.close();
        return membres;
    }
}