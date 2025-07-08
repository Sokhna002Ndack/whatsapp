package org.example.Dao;

import jakarta.persistence.EntityManager;
import org.example.model.Message;
import org.example.util.JpaUtil;

import java.util.List;

public class MessageDao {

    public static void save(Message msg) {
        EntityManager em = JpaUtil.getEntityManager();
        em.getTransaction().begin();
        em.persist(msg);
        em.getTransaction().commit();
        em.close();
    }

    public List<Message> getLastMessages(int limit) {
        EntityManager em = JpaUtil.getEntityManager();
        List<Message> messages = em.createQuery(
                        "SELECT m FROM Message m ORDER BY m.dateEnvoi DESC", Message.class)
                .setMaxResults(limit)
                .getResultList();
        em.close();
        return messages;
    }
}