//$Id$
package org.jboss.seam.jsf;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * A JSF DataModel for arrays.
 * 
 * @author Gavin King
 *
 */
public class ArrayDataModel extends javax.faces.model.ArrayDataModel implements
      Serializable
{
   private static final long serialVersionUID = -1369792328129853864L;
   
   private void writeObject(ObjectOutputStream oos) throws IOException
   {
      oos.writeObject(getWrappedData());
      oos.writeInt(getRowIndex());
   }
   
   private void readObject(ObjectInputStream ois) throws IOException,
         ClassNotFoundException
   {
      this.setWrappedData( ois.readObject() );
      this.setRowIndex( ois.readInt() );
   }
   
   public ArrayDataModel() {}
   
   public ArrayDataModel(Object[] array)
   {
      super(array);
   }
   
}
