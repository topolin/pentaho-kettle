package org.pentaho.di.trans.steps.textfileinput;

import java.io.UnsupportedEncodingException;

import org.pentaho.di.core.Const;

public enum EncodingType {
  SINGLE(1, 0, '\n', '\r'), 
  DOUBLE_BIG_ENDIAN(2, 0xFEFF, 0x000d, 0x000a), 
  DOUBLE_LITTLE_ENDIAN(2, 0xFFFE, 0x0d00, 0x0a00),
  ;
  
  private int length;
  
  /**
   * Byte Order Mark (BOM): http://en.wikipedia.org/wiki/Byte_Order_Mark
   */
  private int bom;
  private int carriageReturnChar;
  private int lineFeedChar;
  
  /**
   * @param length
   * @param bom
   */
  private EncodingType(int length, int bom, int carriageReturnChar, int lineFeedChar) {
    this.length = length;
    this.bom = bom;
    this.carriageReturnChar = carriageReturnChar;
    this.lineFeedChar = lineFeedChar;
  }
  
  public int getLength() {
    return length;
  }
  
  public int getBom() {
    return bom;
  }

  public int getCarriageReturnChar() {
    return carriageReturnChar;
  }
  
  public int getLineFeedChar() {
    return lineFeedChar;
  }
  
  public boolean isReturn(int c) {
    return c==carriageReturnChar || c=='\n';
  }
  
  public boolean isLinefeed(int c) {
    return c==lineFeedChar || c=='\r';
  }
  
  public static EncodingType guessEncodingType(String encoding) {
    
    EncodingType encodingType;
    
    if (Const.isEmpty(encoding)) {
      encodingType=EncodingType.SINGLE;
    } else if (encoding.startsWith("UnicodeBig") || encoding.equals("UTF-16BE")) {
      encodingType = EncodingType.DOUBLE_BIG_ENDIAN; 
    } else if (encoding.startsWith("UnicodeLittle") || encoding.equals("UTF-16LE")) {
      encodingType = EncodingType.DOUBLE_LITTLE_ENDIAN; 
    } else if (encoding.equals("UTF-16")) {
       encodingType = EncodingType.DOUBLE_BIG_ENDIAN; // The default, no BOM
    } else {
      encodingType = EncodingType.SINGLE;
    }

    return encodingType;
  }
  
  public byte[] getBytes(String string, String encoding) throws UnsupportedEncodingException {
    byte[] withBom;
    if (Const.isEmpty(encoding)) {
      withBom = string.getBytes(); 
    } else {
      withBom = string.getBytes(encoding);
    }
    
    switch(length) {
    case 1: return withBom;
    case 2:  
      if (withBom.length<2) return withBom;
      if (withBom[0]<0 && withBom[1]<0) {
        byte[] b = new byte[withBom.length-2];
        for (int i=0;i<withBom.length-2;i++) {
          b[i] = withBom[i+2];
        }
        return b;
      } else {
        return withBom;
      }
    default: return withBom;
    }
  }
}