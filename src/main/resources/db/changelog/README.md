# Liquibase Changelog Directory

This directory contains all database migrations and initial data.

## Structure

- `db.changelog-master.xml` - Master changelog that includes all changes
- `changes/` - Schema migration files (table creation, alterations)
- `data/` - Initial data loading files

## Execution Order

1. Schema changes (001-011) - Creates all tables
2. Data loading (001-005) - Loads initial data

## Adding New Migrations

1. Create new changeset file in `changes/` or `data/`
2. Add include statement to `db.changelog-master.xml`
3. Use sequential IDs (012, 013, etc.)
4. Add preconditions to prevent duplicate execution

## Password Hashes

Initial user passwords in data files are placeholders. The `DataInitializer` will update them with proper BCrypt hashes on first run.

Default passwords:
- admin / admin123
- agent / agent123  
- user / user123

**Important:** Change these passwords in production!

