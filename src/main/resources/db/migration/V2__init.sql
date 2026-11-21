--Migration 2
CREATE TABLE users (
  id             UUID PRIMARY KEY,
  phone          VARCHAR(9) NOT NULL UNIQUE,
  name           VARCHAR(60) NOT NULL,
  password_hash  VARCHAR(100) NOT NULL,
  created_at     BIGINT NOT NULL
);

-- Données de test : elles n'ont pas de propriétaire, on repart de zéro
DELETE FROM operations;
DELETE FROM clients;

ALTER TABLE clients ADD COLUMN user_id UUID NOT NULL REFERENCES users(id);
ALTER TABLE operations ADD COLUMN user_id UUID NOT NULL REFERENCES users(id);

CREATE INDEX idx_clients_user ON clients(user_id, updated_at);
CREATE INDEX idx_operations_user ON operations(user_id, updated_at);