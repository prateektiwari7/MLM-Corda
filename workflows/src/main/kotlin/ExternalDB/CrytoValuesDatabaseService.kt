package ExternalDB

import net.corda.core.node.ServiceHub
import net.corda.core.node.services.CordaService

/**
 * A database service subclass for handling a table of crypto values.
 *
 * @param services The node's service hub.
 */
@CordaService
class CryptoValuesDatabaseService(services: ServiceHub) : DatabaseService(services) {
    init {
        setUpStorage()
    }

    /**
     * Adds a crypto token and associated value to the table of crypto values.
     */
    fun addTokenValue(token: String, value: String) {
        val query = "insert into $TABLE_NAME values(?, ?)"

        val params = mapOf(1 to token, 2 to value)

        executeUpdate(query, params)
        log.info("Token $token added to crypto_values table.")
    }


    /**
     * Retrieves the value of a crypto token in the table of crypto values.
     */
    fun queryTokenValue(token: String): String {
        val query = "select value from $TABLE_NAME where token = ?"

        val params = mapOf(1 to token)

        val results = executeQuery(query, params) { it -> it.getString("value") }

        if (results.isEmpty()) {
            throw IllegalArgumentException("Token $token not present in database.")
        }

        val value = results.single()
        log.info("Token $token read from crypto_values table.")
        return value
    }

    /**
     * Initialises the table of crypto values.
     */
    private fun setUpStorage() {
        val query = """
            create table if not exists $TABLE_NAME(
                token varchar(64),
                value varchar(126)
            )"""

        executeUpdate(query, emptyMap())
        log.info("Created crypto_values table.")
    }


    fun querywholetable(): ArrayList<Any?>  {
        val query = "select * from $TABLE_NAME"


        val results = executeQuery3(query)

        if (results.isEmpty()) {
            throw IllegalArgumentException("Table not present in database.")
        }

        return results
    }




}