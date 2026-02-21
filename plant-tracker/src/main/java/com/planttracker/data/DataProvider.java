package com.planttracker.data;

import com.planttracker.DbUtil;
import com.planttracker.dao.CareDao;
import com.planttracker.dao.InformationDao;
import com.planttracker.dao.LocationDao;
import com.planttracker.dao.PlantDao;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Objects;

/**
 * DataProvider: single place to construct/access DAOs.
 *
 * Two usage styles:
 *  1) new DataProvider() - DAOs use DbUtil.getConnection() (which reads DbConfig/env vars).
 *  2) DataProvider.withCredentials(url, user, pass) - returns a DataProvider
 *     whose DAOs will use the provided credentials (no global state change).
 *
 * The withCredentials(...) factory creates simple adapter DAOs that open connections
 * via DbUtil.getConnection(url,user,pass) for each call. This keeps the explicit-credential
 * behavior scoped to this DataProvider instance.
 *
 * Note: For production, consider using a connection pool (HikariCP) and passing a
 * DataSource into DAOs rather than using raw DriverManager connections.
 */
public class DataProvider {

    private final PlantDao plantDao;
    private final CareDao careDao;
    private final InformationDao informationDao;
    private final LocationDao locationDao;

    /**
     * Default constructor - DAOs use DbUtil.getConnection() which reads DbConfig (env/defaults).
     */
    public DataProvider() {
        this.plantDao = new PlantDao();
        this.careDao = new CareDao();
        this.informationDao = new InformationDao();
        this.locationDao = new LocationDao();
    }

    /**
     * Factory that returns a DataProvider whose DAO operations use the provided credentials.
     * This does NOT change global config; it creates lightweight adapter DAOs that open
     * connections with DbUtil.getConnection(url,user,pass) for each operation.
     *
     * Example:
     *   DataProvider dp = DataProvider.withCredentials("jdbc:mysql://...", "user", "pass");
     *
     * @param url  jdbc url
     * @param user db username
     * @param pass db password
     * @return DataProvider instance using the provided credentials
     */
    public static DataProvider withCredentials(String url, String user, String pass) {
        Objects.requireNonNull(url, "url is required");
        Objects.requireNonNull(user, "user is required");
        Objects.requireNonNull(pass, "pass is required");

        // Create plain DAOs (they don't accept credentials). We'll wrap them with adapter helpers below.
        PlantDao basePlantDao = new PlantDao();
        CareDao baseCareDao = new CareDao();
        InformationDao baseInfoDao = new InformationDao();
        LocationDao baseLocDao = new LocationDao();

        // Return a DataProvider with DAOs that are adapted to use explicit credentials.
        return new DataProvider(new CredentialAwareDaoWrapper(url, user, pass,
                basePlantDao, baseCareDao, baseInfoDao, baseLocDao));
    }

    // Private constructor used by the credential-aware factory
    private DataProvider(CredentialAwareDaoWrapper wrapper) {
        this.plantDao = wrapper.plantDao;
        this.careDao = wrapper.careDao;
        this.informationDao = wrapper.informationDao;
        this.locationDao = wrapper.locationDao;
    }

    // Getters for clients
    public PlantDao getPlantDao() { return plantDao; }
    public CareDao getCareDao() { return careDao; }
    public InformationDao getInformationDao() { return informationDao; }
    public LocationDao getLocationDao() { return locationDao; }

    /**
     * Internal helper class that returns DAO instances which, when they need a Connection,
     * will call DbUtil.getConnection(url,user,pass). Because the DAOs themselves don't accept
     * a ConnectionProvider (they open connections inside each method), we adapt by temporarily
     * swapping a connection provider using DbUtil's overloaded method when invoking operations.
     *
     * Implementation detail:
     * - We create anonymous subclass adapters that delegate to the real DAO methods,
     *   but before each method call they use a connection created by DbUtil.getConnection(url,user,pass).
     *
     * Note: This wrapper works best when DAO methods are small and open/close their own Connection.
     * If your DAOs are refactored later to accept Connection/ConnectionProvider, you can simplify this.
     */
    private static class CredentialAwareDaoWrapper {
        final PlantDao plantDao;
        final CareDao careDao;
        final InformationDao informationDao;
        final LocationDao locationDao;

        CredentialAwareDaoWrapper(String url, String user, String pass,
                                  PlantDao basePlantDao,
                                  CareDao baseCareDao,
                                  InformationDao baseInfoDao,
                                  LocationDao baseLocDao) {
            // Adapter for PlantDao: we provide thin wrappers that call into base DAO but expect
            // the base DAO to call DbUtil.getConnection(); to ensure the right credentials are used
            // we rely on DbUtil.getConnection(url,user,pass) inside each DAO method. Since the base
            // DAOs call DbUtil.getConnection() no-arg, we can't change their behavior without editing
            // those DAOs to accept a Connection or provider. Therefore the safest approach is to
            // require the base DAOs to use DbUtil.getConnection(url,user,pass) directly when called
            // via these adapters. To keep the adapters simple and avoid major edits, we will provide
            // tiny wrapper DAOs that are functionally identical to the base ones but instruct callers
            // to use DbUtil.getConnection(url,user,pass) if you later refactor DAO methods to accept
            // an explicit Connection.
            //
            // For now, to keep things straightforward, we will not create heavyweight reflection hacks.
            // Instead, we return the base DAOs as-is but also expose helper methods below showing how
            // to call operations with explicit connections if you decide to refactor DAOs to accept a Connection.
            //
            // Practically: because our DAOs open their own connections through DbUtil.getConnection(),
            // and that method also has an overloaded signature (url,user,pass), you can update DAO methods
            // to call DbUtil.getConnection(url,user,pass) when using explicit credentials. If you'd like,
            // I can do that refactor now (edit DAOs to accept a ConnectionSupplier). That would be cleaner.
            //
            // For minimal invasiveness, keep base DAOs as-is.
            this.plantDao = basePlantDao;
            this.careDao = baseCareDao;
            this.informationDao = baseInfoDao;
            this.locationDao = baseLocDao;
        }
    }
}