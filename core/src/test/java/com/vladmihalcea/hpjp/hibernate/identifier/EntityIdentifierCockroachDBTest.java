package com.vladmihalcea.hpjp.hibernate.identifier;

import com.vladmihalcea.hpjp.util.AbstractCockroachDBIntegrationTest;
import jakarta.persistence.*;
import org.junit.Test;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class EntityIdentifierCockroachDBTest extends AbstractCockroachDBIntegrationTest {

    @Override
    protected Class<?>[] entities() {
        return new Class<?>[]{
            Post.class,
        };
    }

    @Override
    protected boolean nativeHibernateSessionFactoryBootstrap() {
        return false;
    }

    @Test
    public void test() {
        doInJPA(entityManager -> {
            LocalDate startDate = LocalDate.of(2016, 11, 2);
            for (int offset = 0; offset < 10; offset++) {
                Post post = new Post();
                post.setTitle(
                    String.format(
                        "High-Performance Java Persistence, Review %d",
                        offset
                    )
                );
                post.setCreatedOn(
                    Date.from(startDate
                        .plusDays(offset)
                        .atStartOfDay(ZoneId.of("UTC"))
                        .toInstant()
                    )
                );
                entityManager.persist(post);
            }
        });

        doInJPA(entityManager -> {

            List<Post> posts = entityManager.createQuery(
                "select p " +
                "from Post p " +
                "order by p.createdOn", Post.class)
                .setMaxResults(5)
                .getResultList();

            assertEquals(5, posts.size());
        });
    }

    @Entity(name = "Post")
    @Table(name = "post")
    public static class Post {

        @Id
        @GeneratedValue(
            strategy = GenerationType.IDENTITY
        )
        private Long id;

        @Column
        @Temporal(TemporalType.DATE)
        private Date createdOn;

        private String title;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public Date getCreatedOn() {
            return createdOn;
        }

        public void setCreatedOn(Date createdOn) {
            this.createdOn = createdOn;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }
    }
}