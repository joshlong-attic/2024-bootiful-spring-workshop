drop table if exists authorities;
drop table if exists users;
-- INSTRUCTIONS:
-- start Docker Compose
-- cat legacy-schema.sql | PGPASSWORD=secret psql -U myuser -h localhost mydatabase
--
-- PostgreSQL database dump
--

-- Dumped from database version 16.3 (Debian 16.3-1.pgdg120+1)
-- Dumped by pg_dump version 16.3

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: authorities; Type: TABLE; Schema: public; Owner: myuser
--

CREATE TABLE public.authorities (
    username text NOT NULL,
    authority text NOT NULL
);


ALTER TABLE public.authorities OWNER TO myuser;

--
-- Name: users; Type: TABLE; Schema: public; Owner: myuser
--

CREATE TABLE public.users (
    username text NOT NULL,
    password text NOT NULL,
    enabled boolean NOT NULL
);


ALTER TABLE public.users OWNER TO myuser;

--
-- Data for Name: authorities; Type: TABLE DATA; Schema: public; Owner: myuser
--

COPY public.authorities (username, authority) FROM stdin;
josh	ROLE_USER
rob	ROLE_ADMIN
rob	ROLE_USER
\.


--
-- Data for Name: users; Type: TABLE DATA; Schema: public; Owner: myuser
--

COPY public.users (username, password, enabled) FROM stdin;
josh	843b103f6c09de407d366f8ff6553691767a35dbaf870cad47e86945c4103be85ee8ea49509b9502	t
rob	a94f80ed1a6677dedb489a6912e6c83a7c2a7ada2c4d739dd4a4ab9e454d3d875134fa4480b19c55	t
\.


--
-- Name: users users_pkey; Type: CONSTRAINT; Schema: public; Owner: myuser
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_pkey PRIMARY KEY (username);


--
-- Name: ix_auth_username; Type: INDEX; Schema: public; Owner: myuser
--

CREATE UNIQUE INDEX ix_auth_username ON public.authorities USING btree (username, authority);


--
-- Name: authorities fk_authorities_users; Type: FK CONSTRAINT; Schema: public; Owner: myuser
--

ALTER TABLE ONLY public.authorities
    ADD CONSTRAINT fk_authorities_users FOREIGN KEY (username) REFERENCES public.users(username);


--
-- PostgreSQL database dump complete
--

