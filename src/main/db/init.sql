/*
 * Copyright © 2025 Jim Hamilton.
 * All rights reserved.
 */

create table categories
(
    id   bigserial
        constraint categories_pk
            primary key,
    name text not null
        constraint categories_name
            unique
);

alter table categories
    owner to postgres;

create table recipes
(
    id          bigserial
        primary key,
    name        text                  not null,
    directions  text,
    servings    integer               not null,
    category_id bigint
        constraint recipes_categories_id_fk
            references categories,
    deleted     boolean default false not null,
    deleted_on  timestamp with time zone
);

alter table recipes
    owner to postgres;

create index ix_recipe
    on recipes (name);

create table foods
(
    id          bigserial
        primary key,
    name        text not null,
    description text,
    notes       text
);

alter table foods
    owner to postgres;

create index ix_food
    on foods (name);

create table ingredients
(
    id        bigserial
        constraint ingredients_pk
            primary key,
    item_id   bigint not null
        constraint ingredients_foods_id_fk
            references foods
            on delete restrict,
    amount    double precision,
    unit      text,
    recipe_id bigint
        constraint ingredients_recipes_id_fk
            references recipes
            on delete cascade,
    sort_index integer
);

alter table ingredients
    owner to postgres;

create table preferences
(
    id    bigserial
        constraint preferences_pk
            primary key,
    host  text not null,
    key   text not null,
    value text
);

alter table preferences
    owner to postgres;

create index preferences_host_key_index
    on preferences (host, key);

create table volumes
(
    id     serial
        primary key,
    name   text                  not null,
    in_ml  double precision      not null,
    abbrev text,
    intl   boolean default false not null,
    alt_abbrev text
);

alter table volumes
    owner to postgres;

create index ix_volume
    on volumes (name);

create index ix_vol
    on volumes (abbrev);

create table weights
(
    id       serial
        primary key,
    name     text                  not null,
    in_grams double precision      not null,
    abbrev   text,
    intl     boolean default false not null,
    alt_abbrev text
);

alter table weights
    owner to postgres;

create index ix_weight
    on weights (name);

create index ix_wt
    on weights (abbrev);

create view units(id, name, type, in_base, abbrev, intl, alt_abbrev) as
SELECT volumes.id,
       volumes.name,
       'VOLUME'::text AS type,
       volumes.in_ml  AS in_base,
       volumes.abbrev,
       volumes.intl,
       volumes.alt_abbrev
FROM volumes
UNION
SELECT weights.id,
       weights.name,
       'WEIGHT'::text   AS type,
       weights.in_grams AS in_base,
       weights.abbrev,
       weights.intl,
       weights.alt_abbrev
FROM weights;

alter table units
    owner to postgres;


create function get_category_count(include_deleted boolean)
    returns TABLE(category text, cnt bigint)
    language plpgsql
as
$$
begin
    if include_deleted then
        return query
            select c.name, count(r.id)
            from recipes r
                     join categories c on c.id = r.category_id
            group by c.name
            order by c.name;
    else
        return query
            select c.name, count(r.id)
            from recipes r
                     join categories c on c.id = r.category_id
            where r.deleted is not true
            group by c.name
            order by c.name;
    end if;
end;
$$;

alter function get_category_count(boolean) owner to postgres;

INSERT INTO public.categories (name) VALUES ('Bread');
INSERT INTO public.categories (name) VALUES ('Drinks');
INSERT INTO public.categories (name) VALUES ('Entrées');
INSERT INTO public.categories (name) VALUES ('Desserts');
INSERT INTO public.categories (name) VALUES ('Appetizers');
INSERT INTO public.categories (name) VALUES ('Cookies');
INSERT INTO public.categories (name) VALUES ('Sides');

INSERT INTO public.volumes (name, in_ml, abbrev, intl) VALUES ('cup', 236.5882365, 'c.', false);
INSERT INTO public.volumes (name, in_ml, abbrev, intl) VALUES ('pint', 473.176473, 'pt.', false);
INSERT INTO public.volumes (name, in_ml, abbrev, intl) VALUES ('quart', 946.352946, 'qt.', false);
INSERT INTO public.volumes (name, in_ml, abbrev, intl) VALUES ('gallon', 3785.411784, 'gal.', false);
INSERT INTO public.volumes (name, in_ml, abbrev, intl) VALUES ('fluid ounce', 29.57352956, 'fl. oz.', false);
INSERT INTO public.volumes (name, in_ml, abbrev, intl) VALUES ('teaspoon', 4.92892159, 'tsp.', false);
INSERT INTO public.volumes (name, in_ml, abbrev, intl) VALUES ('firkin', 34068.706056, 'fk', false);
INSERT INTO public.volumes (name, in_ml, abbrev, intl) VALUES ('liter', 1000, 'l', true);
INSERT INTO public.volumes (name, in_ml, abbrev, intl) VALUES ('milliliter', 1, 'ml', true);
INSERT INTO public.volumes (name, in_ml, abbrev, intl, alt_abbrev) VALUES ('tablespoon', 14.78676478, 'tbsp.', false, 'T.');

INSERT INTO public.weights (name, in_grams, abbrev, intl) VALUES ('ounce', 28.34952312, 'oz.', false);
INSERT INTO public.weights (name, in_grams, abbrev, intl) VALUES ('pound', 453.59237, 'lb.', false);
INSERT INTO public.weights (name, in_grams, abbrev, intl) VALUES ('stone', 6350.29318, 'st.', false);
INSERT INTO public.weights (name, in_grams, abbrev, intl) VALUES ('dram', 1.7718452, null, false);
INSERT INTO public.weights (name, in_grams, abbrev, intl) VALUES ('slug', 14593.90293721, null, false);
INSERT INTO public.weights (name, in_grams, abbrev, intl) VALUES ('gram', 1, 'g', true);
INSERT INTO public.weights (name, in_grams, abbrev, intl) VALUES ('kilogram', 1000, 'kg', true);

