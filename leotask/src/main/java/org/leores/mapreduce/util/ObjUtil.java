package org.leores.mapreduce.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.leores.mapreduce.ecpt.*;
import org.leores.mapreduce.util.able.Processable1;
import org.leores.mapreduce.util.able.Processable2;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class ObjUtil extends FileUtil {
	public static Modifier modifier;

	public static int modPub = modifier.PUBLIC;
	public static int modPro = modifier.PROTECTED;
	public static int modPri = modifier.PRIVATE;
	public static int modPubPro = modPub | modPro;
	public static int modPubProPri = modPub | modPro | modPri;
	public static int modAllWithMod = 0x7FFFFFFF;
	public static int modNoMod = 0;
	public static int modAll = -1;//0 means no modifier the default
	public static int modPubNotFinal = -2;

	/**
	 * Check whether class2 is suitable to fill in as a parameter of tClass
	 * 
	 * @param tClass
	 * @param class2
	 * @return
	 * @deprecated use bAssignable instead.
	 */
	public static boolean bMatch(Class tClass, Class class2) {
		boolean rtn = false;

		if (tClass != null && class2 != null) {
			if (tClass == class2) {
				rtn = true;
			} else {
				if (tClass.isInterface()) {
					Class[] c2Interfaces = class2.getInterfaces();
					if (c2Interfaces != null) {
						for (int i = 0; i < c2Interfaces.length; i++) {
							if (tClass == c2Interfaces[i]) {
								rtn = true;
								break;
							}
						}
					}
				}
			}
		}

		return rtn;
	}

	public static boolean bAssignable(Class[] cTos, Class[] cFroms) {
		boolean rtn = false;

		if (cTos != null && cFroms != null && cTos.length == cFroms.length) {
			rtn = true;
			for (int i = 0; i < cTos.length; i++) {
				if (!bAssignable(cTos[i], cFroms[i])) {
					rtn = false;
					break;
				}
			}
		}

		return rtn;
	}

	public static Class[] getClass(Object... objs) {
		Class[] rtn = null;

		if (objs != null) {
			rtn = new Class[objs.length];
			for (int i = 0; i < objs.length; i++) {
				if (objs[i] != null) {
					rtn[i] = objs[i].getClass();
				} else {
					rtn[i] = null;
				}
			}
		}

		return rtn;
	}

	public static boolean bAssignable(Class[] cTos, Object... args) {
		boolean rtn = false;

		Class[] argTypes = getClass(args);
		rtn = bAssignable(cTos, argTypes);

		return rtn;
	}

	public static Method getMethod(Class tClass, String mName, Class... args) {
		Method rtn = null;

		if (tClass != null && mName != null && args != null) {
			Method[] methods = tClass.getMethods();
			for (int i = 0; i < methods.length; i++) {
				String mNamei = methods[i].getName();
				if (mName.equals(mNamei)) {
					Class[] argTypes = methods[i].getParameterTypes();
					if (bAssignable(argTypes, args)) {
						rtn = methods[i];
						break;
					}
				}
			}
		}

		return rtn;
	}

	public static Method getMethod(Class tClass, String mName, Object... args) {
		Method rtn = null;
		Class[] argTypes = getClass(args);
		rtn = getMethod(tClass, mName, argTypes);
		return rtn;
	}

	public static Method getGetterMethod(Class tClass, String sField) {
		String sGetterMethod = getterMethodName(sField);
		Method rtn = getMethod(tClass, sGetterMethod);
		return rtn;
	}

	protected static Method getGetterMethod(Field field) {
		Method rtn = null;
		String sField = field.getName();
		Class tClass = field.getDeclaringClass();
		rtn = getGetterMethod(tClass, sField);
		return rtn;
	}

	protected static Method getGetterMethod(Object obj, String sField) {
		Method rtn = null;
		Class tClass = obj.getClass();
		rtn = getGetterMethod(tClass, sField);
		return rtn;
	}

	public static Method getSetterMethod(Class tClass, String sField, Class... args) {
		String sSetterMethod = setterMethodName(sField);
		Method rtn = getMethod(tClass, sSetterMethod, args);
		return rtn;
	}

	public static Method getSetterMethod(Class tClass, String sField, Object... args) {
		Method rtn = null;
		Class[] argTypes = getClass(args);
		rtn = getSetterMethod(tClass, sField, argTypes);
		return rtn;
	}

	protected static Method getSetterMethod(Field field, Object... args) {
		Method rtn = null;
		String sField = field.getName();
		Class tClass = field.getDeclaringClass();
		rtn = getSetterMethod(tClass, sField, args);
		return rtn;
	}

	protected static Method getSetterMethod(Object obj, String sField, Object... args) {
		Method rtn = null;
		Class tClass = obj.getClass();
		rtn = getSetterMethod(tClass, sField, args);
		return rtn;
	}

	public static Object invokeMethod(boolean bSetAccess, Object obj, Method method, Object... args) {
		Object rtn = null;
		try {
			boolean bAccessible = method.isAccessible();
			if (bSetAccess) {
				method.setAccessible(true);
			}
			rtn = method.invoke(obj, args);
			if (bSetAccess) {
				method.setAccessible(bAccessible);
			}
		} catch (Exception e) {
			tLog(e);
		}
		return rtn;
	}

	public static Object invokeMethod(Object obj, Method method, Object... args) {
		return invokeMethod(true, obj, method, args);
	}

	/**
	 * 
	 * @param tClass
	 * @param obj
	 *            could be null when invoking static class method
	 * @param sMethod
	 * @param bException
	 *            whether send runtime exception when method is not found.
	 * @param args
	 * @return
	 */

	public static Object invokeMethodByName(Class tClass, Object obj, String sMethod, boolean bException, Object... args) {
		Object rtn = null;

		//null check has been done in getMethod.
		Method method = U.getMethod(tClass, sMethod, args);
		if (method != null) {
			rtn = invokeMethod(obj, method, args);
		} else if (bException) {
			throw new TRuntimeException("Can not get [method, args]:", sMethod, args);
		}

		return rtn;
	}

	/**
	 * 
	 * @param obj
	 * @param sMethod
	 * @param bException
	 *            whether send runtime exception when method is not found.
	 * @param args
	 * @return
	 */
	public static Object invokeObjMethodByName(Object obj, String sMethod, boolean bException, Object... args) {
		return invokeMethodByName(obj.getClass(), obj, sMethod, bException, args);
	}

	public static Constructor getConstructor(Class tClass, Class... args) {
		Constructor rtn = null;

		if (tClass != null && args != null) {
			Constructor[] constructors = tClass.getConstructors();
			if (constructors != null) {
				for (int i = 0; i < constructors.length; i++) {
					Class[] argTypes = constructors[i].getParameterTypes();
					if (bAssignable(argTypes, args)) {
						rtn = constructors[i];
						break;
					}
				}
			}
		}

		return rtn;
	}

	public static Constructor getConstructor(Class tClass, Object... args) {
		Constructor rtn = null;

		Class[] argTypes = getClass(args);
		rtn = getConstructor(tClass, argTypes);

		return rtn;
	}

	/**
	 * 
	 * @param tClass
	 * @param args
	 * @return a new instance of tClass if it has a constructor with <b>args</b>
	 *         otherwise <b>null</b>
	 */
	public static Object newInstance(Class tClass, Object... args) {
		Object rtn = null;

		if ((tClass.equals(Integer.class) || tClass.equals(Long.class)) && (args != null && args.length == 1 && args[0] instanceof String)) {
			//Fix the problem that Integer and Long can not be constructed using scientific number e.g. 1E+5.
			//sArg will be converted to an Integer/Long even if it is a decimal.
			String sArg = (String) args[0];
			BigDecimal bd = new BigDecimal(sArg);
			if (tClass.equals(Integer.class)) {
				rtn = bd.intValue();
			} else if (tClass.equals(Long.class)) {
				rtn = bd.longValue();
			}
		} else {
			Constructor tConstructor = getConstructor(tClass, args);
			if (tConstructor != null) {
				try {
					rtn = tConstructor.newInstance(args);
				} catch (Exception e) {
					rtn = null;
					//tLog(e);
				}
			} else {
				// ttLog("Constructor not found for:" + tClass +
				// ObjUtil.toStr(args));
			}
		}

		return rtn;
	}

	/**
	 * 
	 * @param className
	 * @param args
	 * @return a new instance of className if it has a constructor with
	 *         <b>args</b> otherwise <b>null</b>
	 */

	public static Object newInstance(String className, Object... args) {
		Object rtn = null;

		try {
			rtn = newInstance(Class.forName(className), args);
		} catch (ClassNotFoundException e) {
			tLog(e);
		}

		return rtn;
	}

	/**
	 * The use of <b>Arrays.copyOf</b> makes the program requires at least JDK
	 * 1.6. Reprogramme this function would downgrade the requirement to JDK 1.5
	 * 
	 * @param first
	 * @param second
	 * @return
	 */
	public static <T> T[] concat(T[] first, T[] second) {
		T[] rtn = Arrays.copyOf(first, first.length + second.length);
		System.arraycopy(second, 0, rtn, first.length, second.length);
		return rtn;
	}

	public static Field[] concat(Field[] first, Field[] second) {
		Field[] rtn = new Field[first.length + second.length];
		System.arraycopy(first, 0, rtn, 0, first.length);
		System.arraycopy(second, 0, rtn, first.length, second.length);
		return rtn;
	}

	public static boolean bInMod(Field field, Integer mod) {
		boolean rtn = false;
		if (field != null) {
			int modUsed = modPub; // the default mod is modPub.
			if (mod != null) {
				modUsed = mod;
			}
			if (mod > 0) {
				rtn = (modUsed & field.getModifiers()) > 0;
			} else if (mod == 0) {//modNoMod
				rtn = field.getModifiers() == 0;
			} else {//when use modAll = -1
				rtn = true;
			}
		}

		return rtn;
	}

	public static Field[] filterFields(Field[] fields, Integer mod) {
		Field[] rtn = null;

		if (fields != null) {
			rtn = new Field[0];
			List<Field> lMatchedFields = new ArrayList<Field>();
			for (int i = 0; i < fields.length; i++) {
				if (bInMod(fields[i], mod)) {
					lMatchedFields.add(fields[i]);
				}
			}
			rtn = lMatchedFields.toArray(rtn);
		}

		return rtn;
	}

	public static Field[] filterFields(Field[] fields, String[] sFields) {
		Field[] rtn = null;

		if (fields != null && sFields != null) {
			rtn = new Field[0];
			List<Field> lMatchedFields = new ArrayList<Field>();
			for (int i = 0; i < fields.length; i++) {
				if (fields[i] != null) {
					String sFieldi = fields[i].getName();
					for (int j = 0; j < sFields.length; j++) {
						if (sFieldi.equals(sFields[j])) {
							lMatchedFields.add(fields[i]);
							break;
						}
					}
				}
			}
			rtn = lMatchedFields.toArray(rtn);
		}

		return rtn;
	}

	/**
	 * Get the first appearance of the field with the name of <b>sField</b>,
	 * searching from the current Class to its parent Classes.
	 * 
	 * @param tClass
	 * @param sField
	 * @param mod
	 * @return
	 */

	public static Field getField(Class tClass, String sField, Integer mod) {
		Field rtn = null;

		if (sField != null) {
			Field[] fields = getFields(tClass, mod);
			if (fields != null) {
				for (int i = 0; i < fields.length; i++) {
					if (sField.equals(fields[i].getName())) {
						rtn = fields[i];
						break;
					}
				}
			}
		}

		return rtn;
	}

	public static Field[] getFields(Class tClass, Integer mod) {
		Field[] rtn = null;

		if (tClass != null) {
			if (mod == null || mod == modPub) {//null means 
				rtn = tClass.getFields();
			} else {
				//getDecalredFields does not return inherited fields.
				rtn = tClass.getDeclaredFields();
				rtn = filterFields(rtn, mod);
				Class tSuperClass = tClass.getSuperclass();
				if (!tSuperClass.equals(Object.class) && tSuperClass != null) {
					Field[] superFields = getFields(tSuperClass, mod);//recursion to get all fields from all super classes.
					rtn = concat(rtn, superFields);
				}
			}
		}

		return rtn;
	}

	/**
	 * This function gives no warnings if any of the <b>sFields</b> does not
	 * exist. It returns all the fields that exist.
	 * 
	 * @param tClass
	 * @param sFields
	 * @param mod
	 * @return
	 */

	public static Field[] getFields(Class tClass, String[] sFields, Integer mod) {
		Field[] rtn = null;

		if (tClass != null) {
			rtn = getFields(tClass, mod);
			if (sFields != null) {
				rtn = filterFields(rtn, sFields);
			}
		}

		return rtn;
	}

	/**
	 * Get the value of a field. This function firstly attempt to use the getter
	 * method of the field if it exists. If not, it then tries to use reflection
	 * to get the value of the field.
	 * 
	 * @param obj
	 *            can be null when accessing static field.
	 * @param field
	 *            can not be null.
	 * @param bSetAccess
	 *            whether to try to access all field regardless whether it is
	 *            not accessible by normal means. If this is true, the function
	 *            can access private and protected fields.
	 * @return
	 */

	public static Object getFieldValue(Object obj, Field field, boolean bSetAccess, boolean bUseGetter) {
		Object rtn = null;

		if (field != null) {
			boolean getterFound = false;
			if (bUseGetter) {
				//try to find corresponding getter method first.				
				Method mGetter = getGetterMethod(field);
				if (mGetter != null) {
					getterFound = true;
					rtn = invokeMethod(obj, mGetter);
				}
			}
			if (!getterFound) {
				boolean bAccessible = field.isAccessible();
				if (bSetAccess) {
					field.setAccessible(true);
				}
				try {
					rtn = field.get(obj);
				} catch (Exception e) {
					tLog(e);
				}
				if (bSetAccess) {
					field.setAccessible(bAccessible);
				}
			}
		}

		return rtn;
	}

	/**
	 * Get the value of a field. This function firstly attempt to use the getter
	 * method of the field if it exists. If not, it then tries to use reflection
	 * to get the value of the field. By default this function will try to
	 * access inaccessible fields such as private and protected fields.
	 * 
	 * @param obj
	 *            can be null when accessing static field.
	 * @param field
	 *            can not be null.
	 * @return
	 */

	public static Object getFieldValue(Object obj, Field field) {
		return getFieldValue(obj, field, true, true);
	}

	public static Object getFieldValue(Object obj, String sField, Integer mod, boolean bException) {
		Object rtn = null;
		boolean found = false;

		if (U.hasEvaluation(sField)) {
			found = true;
			rtn = U.eval(sField, obj);
		} else if (obj != null) {
			Method mGetter = getGetterMethod(obj, sField);
			if (mGetter != null) {
				found = true;
				rtn = invokeMethod(obj, mGetter);
			} else {
				Field field = getField(obj.getClass(), sField, mod);
				if (field != null) {
					found = true;
					rtn = getFieldValue(obj, field, true, false);
				}
			}
		}

		if (!found && bException) {
			throw new TRuntimeException("Can not get field [obj, sField, mod]:", obj, sField, mod);
		}

		return rtn;
	}

	public static Object getFieldValue(Object obj, String sField, Integer mod) {
		return getFieldValue(obj, sField, mod, true);
	}

	public static Object getFieldValue(Object obj, String sField) {
		return getFieldValue(obj, sField, null, true);
	}

	public static Object[] getFieldValues(Object obj, String[] sFields, Integer mod) {
		Object[] rtn = null;

		if (obj != null && sFields != null) {
			rtn = new Object[sFields.length];
			for (int i = 0; i < sFields.length; i++) {
				rtn[i] = getFieldValue(obj, sFields[i], mod);
			}
		}

		return rtn;
	}

	public static Object[] getFieldValues(Object obj, String[] sFields) {
		return getFieldValues(obj, sFields, null);
	}

	/**
	 * Set the value of a field. This function firstly attempt to use the setter
	 * method of the field if it exists. If not, it then tries to use reflection
	 * to set the value of the field.
	 * 
	 * @param obj
	 *            can be null when accessing static field.
	 * @param field
	 *            can not be null.
	 * @param bSetAccess
	 *            whether to try to access all field regardless whether it is
	 *            not accessible by normal means. If this is true, the function
	 *            can access private and protected fields.
	 * @return
	 */

	public static boolean setFieldValue(Object obj, Field field, Object value, boolean bSetAccess, boolean bUseSetter) {
		boolean rtn = false;
		if (field != null) {
			boolean setterFound = false;
			if (bUseSetter) {
				//try to find corresponding getter method first.
				Method mSetter = getSetterMethod(field, value);
				if (mSetter != null) {
					setterFound = true;
					rtn = true;
					Object mRtn = invokeMethod(obj, mSetter, value);
					if (mRtn != null) {
						rtn = (Boolean) mRtn;
					}
				}
			}
			if (!setterFound) {
				boolean bAccessible = field.isAccessible();
				if (bSetAccess) {
					field.setAccessible(true);
				}
				try {
					rtn = true;
					field.set(obj, value);
				} catch (Exception e) {
					rtn = false;
					tLog(e);
				}
				if (bSetAccess) {
					field.setAccessible(bAccessible);
				}
			}
		}
		return rtn;
	}

	/**
	 * Set the value of a field. This function firstly attempt to use the setter
	 * method of the field if it exists. If not, it then tries to use reflection
	 * to set the value of the field. By default this function will try to
	 * access inaccessible fields such as private and protected fields.
	 * 
	 * @param obj
	 *            can be null when accessing static field.
	 * @param field
	 *            can not be null.
	 * 
	 * @return
	 */

	public static boolean setFieldValue(Object obj, Field field, Object value) {
		return setFieldValue(obj, field, value, true, true);
	}

	/**
	 * 
	 * @param obj
	 * @param sField
	 * @param value
	 * @param bNewInstance
	 *            true: when the value is not directly assignable to the field,
	 *            generate a new instance using the field's class and the value
	 *            as the construct parameter.
	 * @param mod
	 * @return
	 */
	public static boolean setFieldValue(Object obj, String sField, Object value, Integer mod, boolean bNewInstance, boolean bException) {
		boolean rtn = false;

		if (obj != null && sField != null) {
			//try to find corresponding getter method first.
			Method mSetter = getSetterMethod(obj, sField, value);
			if (mSetter != null) {
				rtn = true;
				Object mRtn = invokeMethod(obj, mSetter, value);
				if (mRtn != null) {
					rtn = (Boolean) mRtn;
				}
			} else {
				Field field = getField(obj.getClass(), sField, mod);
				if (field != null) {
					if (value != null && value instanceof String) {
						String sValue = (String) value;
						if (bNoEval(sValue)) {
							value = removeNoEval(sValue);
						} else {
							value = eval(sValue, obj, bException);
						}
					}
					rtn = true;
					Object oValue = value;
					if (bNewInstance && value != null) {
						Class fieldType = field.getType();
						if (!bAssignable(fieldType, value.getClass())) {
							oValue = newInstance(fieldType, value);
							if (oValue == null) {
								rtn = false;
							}
						}
					}
					if (rtn) {
						rtn = setFieldValue(obj, field, oValue, true, false);
					}
				}
			}

		}

		if (!rtn && bException) {
			throw new TRuntimeException("Can not set field value [obj, sField, value, bNewInstance, mod]:", obj, sField, value, bNewInstance, mod);
		}

		return rtn;
	}

	public static boolean setFieldValue(Object obj, String sField, Object value, Integer mod) {
		return setFieldValue(obj, sField, value, mod, false, true);
	}

	public static boolean setFieldValue(Object obj, String sField, Object value) {
		return setFieldValue(obj, sField, value, null, false, true);
	}

	/**
	 * 
	 * @param str
	 *            sSelf (%) represents the object itself.
	 * @param obj
	 * @param flags
	 * @return
	 */
	public static String evalVariable(String str, Object obj, String sPatNumOut, int flags) {
		String rtn = str;

		if (hasVariable(str) && (!bNoEval(str))) {
			if (str.equals("$null$")) {
				rtn = null;
			} else {
				rtn = "";
				Pattern pat = Pattern.compile(sPatVar);
				Matcher matcher = pat.matcher(str);
				int iLastTo = 0;
				while (matcher.find()) {
					int iFrom = matcher.start(), iTo = matcher.end();
					rtn += str.substring(iLastTo, iFrom);
					String sVar = matcher.group(1);
					Object oVal = null;
					boolean bInvalid = false;

					if (sVar.equals(sSelf)) {
						oVal = obj;
					} else if (obj != null) {
						try {
							List<String> lMethod = parseMethodAsStrList(sVar);
							if (lMethod != null && lMethod.size() > 0) {
								String sMethod = lMethod.remove(0);
								if (lMethod.size() > 0) {
									Object[] oaPars = lMethod.toArray();
									oVal = U.invokeObjMethodByName(obj, sMethod, true, oaPars);
								} else {
									oVal = U.invokeObjMethodByName(obj, sMethod, true);
								}

							} else {
								oVal = getFieldValue(obj, sVar, modAll);
							}
						} catch (TRuntimeException e) {
							bInvalid = true;
							if ((flags & EVAL_InvalidException) > 0) {
								throw e;
							}
						}
					}

					if (!bInvalid) {
						if (oVal != null) {
							rtn += valToStr(oVal, sPatNumOut);
						} else {
							if ((flags & EVAL_NullException) > 0) {
								throw new TRuntimeException("evalVariable: null value of ", sVar);
							}
							if ((flags & EVAL_NullIgnore) > 0) {
								rtn += str.substring(iFrom, iTo);
							} else {
								rtn += null;
							}
						}
					} else if ((flags & EVAL_InvalidIgnore) > 0) {
						rtn += str.substring(iFrom, iTo);
					}

					iLastTo = iTo;
				}
				rtn += str.substring(iLastTo);
			}
		}

		return rtn;
	}

	/**
	 * 
	 * @param str
	 *            $variable$ #expression#, e.g. #$var1$+$var2$#. Evaluation
	 *            sections with any whitespace characters (\t\n\x0b\r\f) will be
	 *            ignored. $%$ represents the object itself.
	 * @param obj
	 *            variable values will be obtained from obj.
	 * @param flags
	 * <br>
	 *            <b>EVAL_InvalidException:</b> throw TRuntimeException when
	 *            found invalid evaluations. <br>
	 *            <b>EVAL_InvalidIgnore:</b> ignore those invalid evaluation
	 *            sections including invalid variables (fields/methods) or
	 *            expressions (the original content of those sections will be
	 *            copied to the result). <b>Note</b>: valid variables with null
	 *            values are NOT ignored! <br>
	 *            <b>EVAL_NullException:</b> throw TRuntimeException when a
	 *            evaluation result (field/method) in $xxxx$ is null.<br>
	 *            <b>EVAL_NullIgnore:</b> ignore those $xxx$ evaluation sections
	 *            with null results (the original content of those sections will
	 *            be copied to the result).
	 * 
	 * @return
	 */
	public static String eval(String str, Object obj, String sPatNumOut, int flags) {
		String rtn = evalVariable(str, obj, sPatNumOut, flags);
		rtn = evalExpression(rtn, sPatNumOut, flags); //bIgnoreInvalid and bIgnoreNull are of the same meaning an evaluation of expressions.
		return rtn;
	}

	public static String eval(String str, Object obj, int flags) {
		return eval(str, obj, sPatEvalNumOut, flags);
	}

	public static String eval(String str, Object obj, String sPatNumOut, boolean bException) {
		int flags = 0;
		if (bException) {
			flags = EVAL_InvalidException;
		} else {
			flags = EVAL_InvalidIgnore;
		}
		String rtn = evalVariable(str, obj, sPatNumOut, flags);
		rtn = evalExpression(rtn, sPatNumOut, flags); //bIgnoreInvalid and bIgnoreNull are of the same meaning an evaluation of expressions.
		return rtn;
	}

	public static String eval(String str, Object obj, boolean bException) {
		return eval(str, obj, sPatEvalNumOut, bException);
	}

	public static String evalIterative(String str, Object obj, String sPatNumOut) {
		String rtn = str;

		while (hasEvaluation(rtn)) {
			rtn = eval(rtn, obj, sPatNumOut, EVAL_InvalidException);
		}

		return rtn;
	}

	public static String evalIterative(String str, Object obj) {
		return evalIterative(str, obj, sPatEvalNumOut);
	}

	public static String eval(String str, Object obj, String sPatNumOut) {
		return eval(str, obj, sPatNumOut, EVAL_InvalidException);
	}

	public static String eval(String str, Object obj) {
		return eval(str, obj, sPatEvalNumOut, EVAL_InvalidException);
	}

	/**
	 * Current only support check number expressions. if obj==null, returns
	 * false.
	 * 
	 * @param sCheck
	 * @param obj
	 * @return
	 */
	public static boolean evalCheck(String sCheck, Object obj) {
		boolean rtn = false;
		if (sCheck != null) {
			String sEvaled = U.eval(sCheck, obj);
			BigDecimal bdValid = U.eval1Expression(sEvaled);
			if (bdValid != null && bdValid.intValue() > 0) {
				rtn = true;
			}
		}
		return rtn;
	}

	public static Object clone(Object obj) {
		Object rtn = null;
		if (obj != null) {
			Class oClass = obj.getClass();
			Method mClone = getMethod(oClass, "clone");
			if (mClone != null) {//Class assignable to Object[] will have a clone method. 
				rtn = invokeMethod(obj, mClone);
			} else if (bAssignable(Object[].class, oClass)) {
				Object[] ao = (Object[]) obj;
				rtn = ao.clone();
			} else if (bAssignable(List.class, oClass)) {
				List lo = (List) obj;
				Object[] ao = lo.toArray();
				Object[] aoClone = ao.clone();
				rtn = Arrays.asList(aoClone);
			} else {
				rtn = newInstance(oClass, obj);
			}
		}
		return rtn;
	}

	/**
	 * This function provide a shallow copy of <b>oFrom</b>'s variables values
	 * to <b>oTo</b>'s variables. <br>
	 * <br>
	 * In order to copy a clone of a object rather than its reference, the
	 * object's attributes' class has to implement a constructor that take in an
	 * object of itself as the only parameter or has a clone method. <br>
	 * <br>
	 * The ArrayList and LinkedList has clone methods. So that they could be
	 * copied (cloned) using this function. Arrays can also be copied (cloned)
	 * by this function.
	 * 
	 * @param oTo
	 * @param oFrom
	 * @return
	 */
	public static boolean copy(Object oTo, Object oFrom, String[] sFields, Integer modTo, Integer modFrom) {
		boolean rtn = false;

		if (oTo != null && oFrom != null) {
			rtn = true;
			Class cTo = oTo.getClass();
			Class cFrom = oFrom.getClass();
			if (cTo != cFrom) {
				tLog(ll("LOG_WARNING"), "Copying from a different class!", cTo, cFrom);
			}
			try {
				Field[] fTos = getFields(cTo, sFields, modTo);
				for (int i = 0; i < fTos.length; i++) {
					int fToModifiers = fTos[i].getModifiers();
					if (!modifier.isFinal(fToModifiers)) {
						String fToName = fTos[i].getName();
						Field fFrom = getField(cFrom, fToName, modFrom);
						if (fFrom != null) {
							Object value = getFieldValue(oFrom, fFrom);
							Object vCopy = clone(value);
							boolean bSet;
							if (vCopy != null) {
								bSet = setFieldValue(oTo, fTos[i], vCopy);
							} else {
								bSet = setFieldValue(oTo, fTos[i], value);
							}
							rtn = rtn && bSet;
						}
					}
				}
			} catch (Exception e) {
				rtn = false;
				tLog(e);
			}
		}

		return rtn;
	}

	public static boolean copy(Object oTo, Object oFrom, String[] sFields) {
		return copy(oTo, oFrom, sFields, modAll, modAll);
	}

	public static boolean copy(Object oTo, Object oFrom, String sField) {
		String[] sFields = { sField };
		return copy(oTo, oFrom, sFields, modAll, modAll);
	}

	public static boolean copy(Object oTo, Object oFrom) {
		return copy(oTo, oFrom, null, modAll, modAll);
	}

	public static int processFields(Object tObj, Integer mod, Processable2<Boolean, Object, Field> pa2) {
		int rtn = 0;
		if (tObj != null) {
			Field[] fields = getFields(tObj.getClass(), mod);
			for (int i = 0; i < fields.length; i++) {
				if (pa2.process(tObj, fields[i])) {
					rtn++;
				}
			}
		}
		return rtn;
	}

	public static boolean clear(Object tObj, Integer mod) {
		boolean rtn = false;
		if (tObj != null) {
			rtn = true;
			Field[] fields = getFields(tObj.getClass(), mod);
			for (int i = 0; i < fields.length; i++) {
				if (!setFieldValue(tObj, fields[i], null)) {
					rtn = false;
				}
			}
		}
		return rtn;
	}

	public static boolean clear(Object tObj) {
		return clear(tObj, null);
	}

	public static boolean hasNoNull(Object tObj, Integer mod) {
		boolean rtn = true;

		if (tObj != null) {
			Field[] fields = getFields(tObj.getClass(), mod);
			for (int i = 0; i < fields.length; i++) {
				Object oValue = getFieldValue(tObj, fields[i]);
				if (oValue == null) {
					rtn = false;
					break;
				}
			}
		} else {
			rtn = false;
		}

		return rtn;
	}

	public static boolean hasNoNull(Object tObj) {
		return hasNoNull(tObj, null);
	}

	public static Type[] getActualTypeArguments(Field field) {
		Type[] rtn = null;
		if (field != null) {
			Type gType = field.getGenericType();
			if (gType instanceof ParameterizedType) {
				ParameterizedType pType = (ParameterizedType) gType;
				rtn = pType.getActualTypeArguments();
			}
		}
		return rtn;
	}

	public static Class getComponentType(Field field) {
		Class rtn = null;
		Class fieldType = field.getType();
		if (bAssignable(List.class, fieldType)) {
			Type[] aTypes = getActualTypeArguments(field);
			if (aTypes != null) {
				rtn = (Class) aTypes[0];
			}
		} else if (bAssignable(Object[].class, fieldType)) {
			rtn = fieldType.getComponentType();
		}
		return rtn;
	}

	public static boolean NotNull(Object... objs) {
		boolean rtn = true;

		for (int i = 0; i < objs.length; i++) {
			rtn = rtn && (objs[i] != null);
		}

		return rtn;
	}

	/**
	 * o1 and o2 should not have any loop references!! Otherwise the compare
	 * would not stop. o1 and o2 could be null. null is seemed as the smallest
	 * value. For List and Array, firstly compare size and then each element if
	 * the sizes of o1 and o2 are the same. By setting bHash = false, the
	 * function returns null when o1 and o2 are both not null and there is at
	 * least an Non-NULL elements in o1 or o2 that is not comparable to its
	 * counterpart. By setting bHash = true, the hashcode of o1 and o2 will be
	 * compared in the previous case.
	 * 
	 * @param o1
	 * @param o2
	 * @param bHash
	 *            whether to use Object.hashCode() to compare o1 and o2 when
	 *            they are not able to be compared (will return null if bHash =
	 *            false in this case)
	 * @return 1 if o1 > o2, -1 if o1 < o2, 0 if o1 = o2, null if unable to
	 *         compare.
	 */

	public static Integer compare(Object o1, Object o2, boolean bHash) {
		Integer rtn = null;

		if (o1 != null && o2 != null) {
			Class cO1 = o1.getClass();
			Class cO2 = o2.getClass();
			if (bAssignable(cO1, cO2) && bAssignable(Comparable.class, cO1)) {
				Comparable c1 = (Comparable) o1;
				Comparable c2 = (Comparable) o2;
				rtn = c1.compareTo(c2);
			} else if (bAssignable(List.class, cO1) && bAssignable(List.class, cO2)) {
				List l1 = (List) o1;
				List l2 = (List) o2;
				int size1 = l1.size();
				int size2 = l2.size();
				if (size1 == size2) {
					rtn = 0;
					//this loop will stop when rtn !=0, including the situation when rtn == null
					for (int i = 0; i < size1 && rtn == 0; i++) {
						Object eO1 = l1.get(i);
						Object eO2 = l2.get(i);
						rtn = compare(eO1, eO2, bHash);
					}
				} else if (size1 > size2) {
					rtn = 1;
				} else {
					rtn = -1;
				}
			} else if (bAssignable(Object[].class, cO1) && bAssignable(Object[].class, cO2)) {
				Object[] oa1 = (Object[]) o1;
				Object[] oa2 = (Object[]) o2;
				if (oa1.length == oa2.length) {
					rtn = 0;
					//this loop will stop when rtn !=0, including the situation when rtn == null
					for (int i = 0; i < oa1.length && rtn == 0; i++) {
						rtn = compare(oa1[i], oa2[i], bHash);
					}
				} else if (oa1.length > oa2.length) {
					rtn = 1;
				} else {
					rtn = -1;
				}
			} else if (bHash) {
				Integer iO1 = o1.hashCode();
				Integer iO2 = o2.hashCode();
				rtn = iO1.compareTo(iO2);
			}
		} else if (o1 == null && o2 == null) {
			rtn = 0;
		} else if (o1 != null) {
			rtn = 1;
		} else {
			rtn = -1;
		}

		return rtn;
	}

	/**
	 * o1 and o2 should not have any loop references!! Otherwise the compare
	 * would not stop. o1 and o2 could be null. null is seemed as the smallest
	 * value. For List and Array, firstly compare size and then each element if
	 * the sizes of o1 and o2 are the same. The function compares the hashcode
	 * of o1 and o2 when they are both not null and there is at least a Non-NULL
	 * elements in o1 or o2 that is not comparable to its counterpart.
	 * 
	 * @param o1
	 * @param o2
	 * @return 1 if o1 > o2, -1 if o1 < o2, 0 if o1 = o2.
	 */

	public static int compare(Object o1, Object o2) {
		return compare(o1, o2, true);
	}

	public static boolean bValidNumber(Object o1) {
		boolean rtn = false;
		if (o1 != null && o1 instanceof Number) {
			Number n1 = (Number) o1;
			double d1 = n1.doubleValue();
			rtn = !(Double.isInfinite(d1) || Double.isNaN(d1));
		}
		return rtn;
	}

	public static boolean bValidToNumber(Object o1) {
		boolean rtn = false;
		if (o1 != null) {
			Object o1c = o1;
			if (o1c instanceof String) {
				o1c = toDouble(o1c + "");
			}
			rtn = bValidNumber(o1c);
		}
		return rtn;
	}

	/**
	 * Test if the o1 is a instance of Number or a String but can be convert to
	 * a Number.
	 * 
	 * @param o1
	 * @return
	 */
	public static boolean bNumber(Object o1) {
		boolean rtn = false;
		if (o1 != null) {
			Object o1c = o1;
			if (o1c instanceof String) {
				o1c = toDouble(o1c + "");
			}
			rtn = o1c != null && o1c instanceof Number;
		}
		return rtn;
	}

	/**
	 * 
	 * @param obj
	 * @return null when obj is null, otherwise obj.hashCode()
	 */
	public static Integer hash(Object obj) {
		Integer rtn = null;
		if (obj != null) {
			rtn = obj.hashCode();
		}
		return rtn;
	}
}
