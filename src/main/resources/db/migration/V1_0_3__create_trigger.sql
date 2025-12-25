CREATE OR REPLACE FUNCTION set_update_time()
    RETURNS TRIGGER AS
$$
BEGIN
    NEW.update_time = now();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_api_interface_update
    BEFORE UPDATE
    ON api_gateway.api_interface
    FOR EACH ROW
EXECUTE FUNCTION set_update_time();

CREATE TRIGGER trg_api_interface_version_update
    BEFORE UPDATE
    ON api_gateway.api_interface_version
    FOR EACH ROW
EXECUTE FUNCTION set_update_time();
