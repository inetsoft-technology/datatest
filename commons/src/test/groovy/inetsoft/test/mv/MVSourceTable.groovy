package inetsoft.test.mv

import inetsoft.sree.security.SRPrincipal
import inetsoft.uql.jdbc.JDBCDataSource
import inetsoft.uql.jdbc.JDBCHandler
import inetsoft.uql.service.DataSourceRegistry

import java.sql.Connection

/**
 * A table that a case creates in the database of a JDBC data source and changes between the MV
 * creation and the incremental updates, so that an update has new rows to append and old rows
 * to delete. Meant for the local H2 databases of the mv module ({@code mvtest} =
 * {@code mvorder1}), never for a shared database. {@link #drop()} it when the case ends.
 * <p>
 * The statements run on a connection of the data source's pool: the H2 driver is only on the
 * class path of the plugin, not of the test. The pool turns auto-commit off, so each statement
 * is committed.
 */
class MVSourceTable {
   MVSourceTable(String dataSource, String table, SRPrincipal principal) {
      this.dataSource = dataSource
      this.table = table
      this.principal = principal
   }

   String getName() {
      return table
   }

   /**
    * (Re)create the table empty, with the columns of an existing table.
    */
   void createLike(String source) {
      drop()
      execute("CREATE TABLE ${table} AS SELECT * FROM ${source} WHERE 1 = 0")
   }

   void drop() {
      execute("DROP TABLE IF EXISTS ${table}")
   }

   void execute(String statement) {
      def ds = (JDBCDataSource) DataSourceRegistry.getRegistry().getDataSource(dataSource)

      if(ds == null) {
         throw new IllegalStateException("data source not found: " + dataSource)
      }

      Connection conn = new JDBCHandler().getConnection(ds, principal)

      try {
         conn.createStatement().withCloseable { it.execute(statement) }

         if(!conn.getAutoCommit()) {
            conn.commit()
         }
      }
      finally {
         conn.close()
      }
   }

   private final String dataSource
   private final String table
   private final SRPrincipal principal
}
