create table processed_events (
    event_id uuid primary key,
    email varchar(255) not null,
    operation varchar(50) not null,
    status varchar(50) not null,
    processed_at timestamp,
    error_message varchar(1000)
);
