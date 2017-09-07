package org.keyple.calypso.commandset;

import java.util.Arrays;


/**
 * The Class byteArray.
 *
 * @author Ixxi
 */
public class ByteArray {

    /** The value. */
    byte[] value = new byte[] {};

    /**
     * Instantiates a new byteArray.
     *
     * @param bytes the bytes
     */
    public ByteArray(byte... bytes) {
        value = bytes.clone();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.hashCode(value);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
    	boolean isEquals = false;
    	if (this == obj)
    		isEquals = true;
    	if(obj != null){
    		ByteArray other = (ByteArray) obj;
    		if (!Arrays.equals(value, other.value) || getClass() != obj.getClass()){
    			isEquals = false;
    		}
    		
    	}
        return isEquals;
    }
}
