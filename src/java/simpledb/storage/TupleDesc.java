package simpledb.storage;

import simpledb.common.Type;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * TupleDesc describes the schema of a tuple.
 */
public class TupleDesc implements Serializable {

    public final TDItem[] desc;

    /**
     * A help class to facilitate organizing the information of each field
     */
    public static class TDItem implements Serializable {

        private static final long serialVersionUID = 1L;

        /**
         * The type of the field
         */
        public final Type fieldType;

        /**
         * The name of the field
         */
        public final String fieldName;

        public TDItem(Type t, String n) {
            this.fieldName = n;
            this.fieldType = t;
        }

        public String toString() {
            return fieldName + "(" + fieldType + ")";
        }
    }

    /**
     * @return An iterator which iterates over all the field TDItems
     *         that are included in this TupleDesc
     */
    public Iterator<TDItem> iterator() {
        return Arrays.asList(this.desc).iterator();
    }

    private static final long serialVersionUID = 1L;

    /**
     * Create a new TupleDesc with typeAr.length fields with fields of the
     * specified types, with associated named fields.
     *
     * @param typeAr  array specifying the number of and types of fields in this
     *                TupleDesc. It must contain at least one entry.
     * @param fieldAr array specifying the names of the fields. Note that names may
     *                be null.
     */
    public TupleDesc(Type[] typeAr, String[] fieldAr) {
        this.desc = new TDItem[typeAr.length];
        for (int i = 0; i < typeAr.length; i++) {
            this.desc[i] = new TDItem(typeAr[i], fieldAr[i]);
        }
    }

    /**
     * Constructor. Create a new tuple desc with typeAr.length fields with
     * fields of the specified types, with anonymous (unnamed) fields.
     *
     * @param typeAr array specifying the number of and types of fields in this
     *               TupleDesc. It must contain at least one entry.
     */
    public TupleDesc(Type[] typeAr) {
        this.desc = new TDItem[typeAr.length];
        //use null as unnamed field
        for (int i = 0; i < typeAr.length; i++) {
            this.desc[i] = new TDItem(typeAr[i], null);
        }
    }

    /**
     * @return the number of fields in this TupleDesc
     */
    public int numFields() {
        return this.desc.length;
    }

    /**
     * Gets the (possibly null) field name of the ith field of this TupleDesc.
     *
     * @param i index of the field name to return. It must be a valid index.
     * @return the name of the ith field
     * @throws NoSuchElementException if i is not a valid field reference.
     */
    public String getFieldName(int i) throws NoSuchElementException {
        //index needs to be less than the number of fields
        if (i >= this.desc.length) {
            throw new NoSuchElementException();
        } 
        return this.desc[i].fieldName;
    }

    /**
     * Gets the type of the ith field of this TupleDesc.
     *
     * @param i The index of the field to get the type of. It must be a valid
     *          index.
     * @return the type of the ith field
     * @throws NoSuchElementException if i is not a valid field reference.
     */
    public Type getFieldType(int i) throws NoSuchElementException {
        //index needs to be less than the number of fields
        if (i >= this.desc.length) {
            throw new NoSuchElementException();
        } 
        return this.desc[i].fieldType;
    }

    /**
     * Find the index of the field with a given name.
     *
     * @param name name of the field.
     * @return the index of the field that is first to have the given name.
     * @throws NoSuchElementException if no field with a matching name is found.
     */
    public int indexForFieldName(String name) throws NoSuchElementException {
        for (int i = 0; i < this.desc.length; i++) {
            if (this.desc[i].fieldName != null && this.desc[i].fieldName.equals(name)) {
                return i;
            }
        }
        throw new NoSuchElementException();
    }

    /**
     * @return The size (in bytes) of tuples corresponding to this TupleDesc.
     *         Note that tuples from a given TupleDesc are of a fixed size.
     */
    public int getSize() {
        int size = 0;
        for (int i = 0; i < this.desc.length; i++) {
            if (this.desc[i].fieldType.equals(Type.INT_TYPE)) {
                size = size + Type.INT_TYPE.getLen();
            }
            else if (this.desc[i].fieldType.equals(Type.STRING_TYPE)) {
                size = size + Type.STRING_TYPE.getLen();
            }
        }
        return size;
    }

    /**
     * Merge two TupleDescs into one, with td1.numFields + td2.numFields fields,
     * with the first td1.numFields coming from td1 and the remaining from td2.
     *
     * @param td1 The TupleDesc with the first fields of the new TupleDesc
     * @param td2 The TupleDesc with the last fields of the TupleDesc
     * @return the new TupleDesc
     */
    public static TupleDesc merge(TupleDesc td1, TupleDesc td2) {

        Type[] fieldTypes = new Type[td1.desc.length + td2.desc.length];
        String[] fieldNames = new String[td1.desc.length + td2.desc.length];
        for (int i = 0; i < td1.desc.length; i++) {
            fieldNames[i] = td1.desc[i].fieldName;
            fieldTypes[i] = td1.desc[i].fieldType;
        }
        int current = td1.desc.length;
        for (int i = 0; i < td2.desc.length; i++) {
            fieldNames[current + i] = td2.desc[i].fieldName;
            fieldTypes[current + i] = td2.desc[i].fieldType;
        }
        return new TupleDesc(fieldTypes, fieldNames);
    }

    /**
     * Compares the specified object with this TupleDesc for equality. Two
     * TupleDescs are considered equal if they have the same number of items
     * and if the i-th type in this TupleDesc is equal to the i-th type in o
     * for every i.
     *
     * @param o the Object to be compared for equality with this TupleDesc.
     * @return true if the object is equal to this TupleDesc.
     */

    public boolean equals(Object o) {
        if (o.getClass() != this.getClass()) {
            return false;
        } 

        TupleDesc obj = (TupleDesc) o;
        if (obj.desc.length != this.desc.length) {
            return false;
        } else {
            for (int i = 0; i < this.desc.length; i++) {
                if (obj.desc[i].fieldName != this.desc[i].fieldName || obj.desc[i].fieldType != this.desc[i].fieldType) {
                    return false;
                }
            }
        }
        return true;
    }

    public int hashCode() {
        // If you want to use TupleDesc as keys for HashMap, implement this so
        // that equal objects have equals hashCode() results
        int value = 0;
        for (int i = 0; i < this.desc.length; i++) {
            value = value + i * (this.desc[i].fieldName.hashCode() + this.desc[i].fieldType.hashCode());
        }
        return value;
    }

    /**
     * Returns a String describing this descriptor. It should be of the form
     * "fieldType[0](fieldName[0]), ..., fieldType[M](fieldName[M])", although
     * the exact format does not matter.
     *
     * @return String describing this descriptor.
     */
    public String toString() {
        // TODO: some code goes here
        String tempString = "";
        for (int i = 0; i < this.desc.length; i++) {
            tempString = tempString + (this.desc[i].fieldName + "(" + this.desc[i].fieldType + ")" + "\n");
        }
        return tempString;
    }
}
