package com.vladmihalcea.hpjp.util.providers.entity;

import com.vladmihalcea.hpjp.util.EntityProvider;

import jakarta.persistence.*;

/**
 * @author Vlad Mihalcea
 */
public class AutoIncrementBatchEntityProvider implements EntityProvider {

    @Override
    public Class<?>[] entities() {
        return new Class<?>[]{
                Post.class
        };
    }

    @Entity(name = "post")
    public static class Post {

        @Id
        @GeneratedValue(strategy = GenerationType.AUTO)
        private Long id;

        private String title;

        @Version
        private short version;

        private Post() {
        }

        public Post(String title) {
            this.title = title;
        }

        public void setTitle(String title) {
            this.title = title;
        }
    }
}
