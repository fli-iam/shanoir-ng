Shanoir-ng migrations directory
===============================

SQL migrations scripts are stored as: `<DB_NAME>/<SEQ_NUMBER>_<DESCRIPTION>.sql` and included in the
*database* Docker image.

They can be applied on an existing Shanoir-NG instance by running the *database* container with
`SHANOIR_MIGRATION=manual`. See the `shanoir-entrypoint.sh` script for more details.

Migrations are applied in the alphebetical order. Collisions on sequence number may arise (if two
migrations are created independently on two different branches), but are not problematic. The main
point is to ensure that they are applied in a deterministic order.

Important: migrations are tracked by their filename. **Do not rename a migration after a release**
(or the existing shanoir-ng instances would attempt to apply it a second time).
