package dao;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import model.Player;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.lang.reflect.Type;
import java.util.List;

public class PlayerDAOImpl implements PlayerDAO{

    @Override
    public void setOrUpdate(Player player) {
        Session session = HibernateSessionFactoryUtil.getSessionFactory().openSession();
        Transaction tx = session.beginTransaction();
        session.merge(player);
        tx.commit();
        session.close();
    }

    @Override
    public Player getByName(String name) {
        return HibernateSessionFactoryUtil.getSessionFactory().openSession().get(Player.class, name);
    }

    @Override
    public List<Player> getAll() {
        Session session = HibernateSessionFactoryUtil.getSessionFactory().openSession();
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<Player> criteria = builder.createQuery(Player.class);
        criteria.from(Player.class);
        return session.createQuery(criteria).getResultList();
    }
}
