package com.interview.repository;

import com.interview.domain.entity.User;
import com.interview.exception.RepositoryException;
import com.interview.util.HibernateUtil;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.List;
import java.util.Optional;

@Slf4j
public class UserRepository {

    public User save(User user) {
        Transaction transaction = null;

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();

            session.persist(user);

            transaction.commit();
            return user;
        } catch (HibernateException e) {
            rollback(transaction);
            log.error("Failed to save user with email {}", user.getEmail(), e);
            throw new RepositoryException("Failed to save user", e);
        }
    }

    public Optional<User> findById(Long id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return Optional.ofNullable(session.get(User.class, id));
        } catch (HibernateException e) {
            log.error("Failed to find user by id {}", id, e);
            throw new RepositoryException("Failed to find user by id", e);
        }
    }

    public List<User> findAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session
                    .createQuery("from User", User.class)
                    .list();
        } catch (HibernateException e) {
            log.error("Failed to find all users", e);
            throw new RepositoryException("Failed to find users", e);
        }
    }

    public Optional<User> update(Long id, String name, String email, Integer age) {
        Transaction transaction = null;

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();

            User user = session.get(User.class, id);

            if (user == null) {
                transaction.commit();
                return Optional.empty();
            }

            user.setName(name);
            user.setEmail(email);
            user.setAge(age);

            transaction.commit();
            return Optional.of(user);
        } catch (HibernateException e) {
            rollback(transaction);
            log.error("Failed to update user with id {}", id, e);
            throw new RepositoryException("Failed to update user", e);
        }
    }

    public boolean deleteById(Long id) {
        Transaction transaction = null;

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();

            User user = session.get(User.class, id);

            if (user == null) {
                transaction.commit();
                return false;
            }

            session.remove(user);

            transaction.commit();
            return true;
        } catch (HibernateException e) {
            rollback(transaction);
            log.error("Failed to delete user with id {}", id, e);
            throw new RepositoryException("Failed to delete user", e);
        }
    }

    private void rollback(Transaction transaction) {
        if (transaction != null && transaction.isActive()) {
            transaction.rollback();
        }
    }
}
