package nhandler.conversion.jpf2jvm;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import gov.nasa.jpf.vm.ClassInfo;
import gov.nasa.jpf.vm.DynamicElementInfo;
import gov.nasa.jpf.vm.FieldInfo;
import gov.nasa.jpf.vm.MJIEnv;
import gov.nasa.jpf.vm.StaticElementInfo;
import nhandler.conversion.ConversionException;
import nhandler.conversion.ConverterBase;

public class JPF2JVMGenericConverter extends JPF2JVMConverter {
  
  @Override
  protected void setJVMClassFields(Class<?> JVMCls, StaticElementInfo sei, MJIEnv env) throws ConversionException {
    ClassInfo ci = sei.getClassInfo();
    while (JVMCls!=null) {
      Field fld[] = JVMCls.getDeclaredFields();

      for (int i = 0; i < fld.length; i++) {
        boolean isStatic = ((Modifier.toString(fld[i].getModifiers())).indexOf("static") != -1);
        boolean isFinal = ((Modifier.toString(fld[i].getModifiers())).indexOf("final") != -1);

        // Provide access to private and final fields
        fld[i].setAccessible(true);
        FieldInfo fi = sei.getFieldInfo(fld[i].getName());

        // For class only set the values of static fields
        if (fi != null && isStatic) {
          /**
           * Why we check for !(isFinal)?
           * 
           * We do not set the value for "static final" fields. But we take
           * care of "non-static final" fields.
           * 
           * static final fields can be initialized at the declaration time,
           * OW it MUST be initialized inside the static block. By using
           * Class.forName() the class is initialized. Since when the class
           * is initialized the static blocks are executed, the static final
           * fields of object returned by Class.forName() have already have
           * the right values and we do not need to update their value.
           * 
           * Non-static final fields can be initialized at the declaration
           * time. But if the non-static field is final blank, it MUST be
           * initialized in the constructor. By using Class.newInstance()
           * the class is instantiated as if by a new expression with an
           * empty argument list. If the object represented by JPFRef
           * created using different constructor, the value of final blank
           * fields might be different when using the constructor with an
           * empty argument list. Therefore the values of non-static final
           * fields have to be set.
           */
          if (!isFinal) {
            // If the current field is of reference type
            if (fi.isReference()) {
              int fieldValueRef = sei.getFields().getReferenceValue(fi.getStorageOffset());
              Object JVMField = obtainJVMObj(fieldValueRef, env);
              try {
                fld[i].set(null, JVMField);
              } catch (IllegalArgumentException e) {
                e.printStackTrace();
              } catch (IllegalAccessException e) {
                e.printStackTrace();
              }
            }
            // If the current field is of primitive type
            else {
              try {
                ConverterBase.setJVMPrimitiveField(fld[i], JVMCls, sei, fi);
              } catch (IllegalAccessException e) {
                e.printStackTrace();
              }
            }
          }
        }
      }

      JVMCls = JVMCls.getSuperclass();
      ci = ci.getSuperClass();
    }
  }

  @Override
  protected void setJVMObjFields(Object JVMObj, DynamicElementInfo dei, MJIEnv env) throws ConversionException {
    Class<?> cls = JVMObj.getClass();
    ClassInfo JPFCl = dei.getClassInfo();

    while (cls!=null) {
      Field fld[] = cls.getDeclaredFields();

      for (int i = 0; i < fld.length; i++) {

        // It is true if the field is declared as static.
        boolean isNonStaticField = ((Modifier.toString(fld[i].getModifiers())).indexOf("static") == -1);

        // Provide access to private and final fields
        fld[i].setAccessible(true);
        FieldInfo fi = JPFCl.getInstanceField(fld[i].getName());

        if (fi != null && isNonStaticField) {
          // Field is of reference type
          if (fi.isReference()) {
            int fieldValueRef = dei.getFields().getReferenceValue(fi.getStorageOffset());
            Object JVMField = obtainJVMObj(fieldValueRef, env);

            try {
              fld[i].set(JVMObj, JVMField);
            } catch (IllegalArgumentException e) {
              e.printStackTrace();
            } catch (IllegalAccessException e) {
              e.printStackTrace();
            }
          }
          // Field is of primitive type
          else {
            try {
              ConverterBase.setJVMPrimitiveField(fld[i], JVMObj, dei, fi);
            } catch (IllegalAccessException e) {
              e.printStackTrace();
            }
          }
        }
      }
      cls = cls.getSuperclass();
      JPFCl = JPFCl.getSuperClass();
    }
  }
}
