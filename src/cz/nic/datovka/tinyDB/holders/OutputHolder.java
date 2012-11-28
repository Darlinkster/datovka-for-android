package cz.nic.datovka.tinyDB.holders;

import java.io.IOException;

/**
 *
 * Interface pro čtení obsahu XML elementu.
 * 
 * @author Vaclav Rosecky <xrosecky 'at' gmail 'dot' com>
 */
public interface OutputHolder<T> {

    public void write(char[] array, int start, int length) throws IOException;
    
    /**
     * Vrátí výsledek čtení.
     */ 
    public T getResult();
}
