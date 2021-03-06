package mcp.mobius.opis.data.holders.basetypes;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;

import mcp.mobius.opis.data.holders.ISerializable;
import mcp.mobius.opis.data.holders.newtypes.CachedString;

public class SerialString implements ISerializable {

	public String value = "";
	
	public SerialString(String value){
		this.value = value;
	}
	
	@Override
	public void writeToStream(ByteArrayDataOutput stream){
		stream.writeUTF(this.value);
	}

	public static  SerialString readFromStream(ByteArrayDataInput stream){
		return new SerialString(stream.readUTF());
	}	
}
