package com.vladmihalcea.hpjp.hibernate.identifier;

import com.vladmihalcea.hpjp.util.AbstractTest;
import org.hibernate.Session;
import org.hibernate.annotations.GenericGenerator;
import org.junit.Test;

import jakarta.persistence.*;
import java.sql.Statement;

public class AssignedIdentityGeneratorTest extends AbstractTest {

    @Override
    protected Class<?>[] entities() {
        return new Class<?>[] {
                Post.class,
        };
    }

    @Test
    public void test() {
        LOGGER.debug("test");

        doInJPA(entityManager -> {
            Session session = entityManager.unwrap(Session.class);
            session.doWork(connection -> {
                try(Statement statement = connection.createStatement()) {
                    statement.executeUpdate("ALTER TABLE post ALTER COLUMN id bigint generated by default as identity (start with 1)");
                }
            });
        });

        doInJPA(entityManager -> {
            entityManager.persist(new Post());
            entityManager.persist(new Post(-1L));
            entityManager.persist(new Post());
            entityManager.persist(new Post(-2L));
        });
    }

    @Entity(name = "Post")
    @Table(name = "post")
    public static class Post implements Identifiable<Long> {

        @Id
        @GenericGenerator(
            name = "assigned-identity",
            strategy = "com.vladmihalcea.hpjp.hibernate.identifier.AssignedIdentityGenerator"
        )
        @GeneratedValue(generator = "assigned-identity", strategy = GenerationType.IDENTITY)
        private Long id;

        @Version
        private Short version;

        public Post() {
        }

        public Post(Long id) {
            this.id = id;
        }

        @Override
        public Long getId() {
            return id;
        }
    }

}