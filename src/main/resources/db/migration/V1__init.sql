CREATE TABLE clients (
  id          UUID PRIMARY KEY,
  name        VARCHAR(60) NOT NULL,
  phone       VARCHAR(20),
  address     VARCHAR(100),
  note        VARCHAR(200),
  created_at  BIGINT NOT NULL,
  updated_at  BIGINT NOT NULL,
  deleted_at  BIGINT
);

CREATE TABLE operations (
  id          UUID PRIMARY KEY,
  client_id   UUID NOT NULL REFERENCES clients(id),
  type        VARCHAR(10) NOT NULL CHECK (type IN ('DEBT', 'PAYMENT')),
  amount      BIGINT NOT NULL CHECK (amount > 0),
  label       VARCHAR(80),
  due_date    BIGINT,
  created_at  BIGINT NOT NULL,
  updated_at  BIGINT NOT NULL,
  deleted_at  BIGINT
);

CREATE INDEX idx_operations_client ON operations(client_id, created_at);