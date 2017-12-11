package org.keyple.commands.calypso.utils;

public class TestsUtilsStatusCodeGenerator {

    public static byte[] generateSuccessfulStatusCode(){
        return new byte[]{ (byte) 0x90, 0x00 }; 
    }
    
    public static byte[] generateCommandForbiddenOnBinaryFilesStatusCode(){
        return new byte[]{ (byte) 0x69, (byte) 0x81 }; 
    }
    
    public static byte[] generateFileNotFoundStatusCode(){
        return new byte[]{ (byte) 0x69, (byte) 0x82 }; 
    }
    
    public static byte[] generateRecordNotFoundStatusCode(){
        return new byte[]{ (byte) 0x6A, (byte) 0x83 }; 
    }
    
    public static byte[] generateP2ValueNotSupportedStatusCode(){
        return new byte[]{ (byte) 0x6B, 0x00 }; 
    }
    
    public static byte[] generateLeValueIncorrectStatusCode(){
        return new byte[]{ (byte) 0x6C, (byte) 0xFF }; 
    }
    
    public static byte[] generateAccessForbiddenStatusCode(){
        return new byte[]{ (byte) 0x69, (byte) 0x85 }; 
    }
}
