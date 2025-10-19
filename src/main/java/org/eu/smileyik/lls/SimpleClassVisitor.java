package org.eu.smileyik.lls;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.*;

import static org.objectweb.asm.Opcodes.ASM9;

public class SimpleClassVisitor extends ClassVisitor {
    private static final Map<String, String> BASE_TYPE_MAP;
    private static final List<String> ACCESS_SCOPE = Arrays.asList(
            "ACC_PUBLIC",
            "ACC_PRIVATE",
            "ACC_PROTECTED",
            "ACC_STATIC",
            "ACC_FINAL",
            "ACC_NATIVE"
    );

    static {
        Map<String, String> map = new HashMap<>();
        map.put("Z", "boolean");
        map.put("B", "byte");
        map.put("I", "int");
        map.put("F", "float");
        map.put("D", "double");
        map.put("J", "long");
        map.put("S", "short");
        map.put("V", "void");
        map.put("C", "char");
        BASE_TYPE_MAP = Collections.unmodifiableMap(map);
    }

    public SimpleClassVisitor() {
        super(ASM9);
    }

    public SimpleClassVisitor(int api, ClassVisitor classVisitor) {
        super(api, classVisitor);
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        System.out.println(version + " " + access2StringList(access) + " " + name + " " + signature + " " + superName + " " + Arrays.toString(interfaces));
        super.visit(version, access, name, signature, superName, interfaces);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        System.out.println("method " + access2StringList(access) + " " + name + " @" + descriptor + " " + getMethodParams(descriptor) + " @" + signature + " " + getReturnType(descriptor) + " " + Arrays.toString(exceptions));
        return super.visitMethod(access, name, descriptor, signature, exceptions);
    }

    @Override
    public void visitOuterClass(String owner, String name, String descriptor) {
        System.out.println("outerClass" + owner + " " + name + " " + descriptor);
        super.visitOuterClass(owner, name, descriptor);
    }

    @Override
    public void visitInnerClass(String name, String outerName, String innerName, int access) {
        System.out.println("innerClass " + name + " " + outerName + " " + innerName + " " + access);
        super.visitInnerClass(name, outerName, innerName, access);
    }

    protected List<String> access2StringList(int access) {

        List<String> list = new ArrayList<String>();
        Class<Opcodes> opcodesClass = Opcodes.class;
        ACCESS_SCOPE.forEach(it -> {
            try {
                Field field = opcodesClass.getDeclaredField(it);
                int o = ((Number) field.get(null)).intValue();
                if ((access & o) != 0) {
                    list.add(it.substring(4).toLowerCase(Locale.ENGLISH));
                }
            } catch (Exception e) {
                return;
            }
        });
        return list;
    }

    protected List<String> getMethodParams(String methodDescriptor) {
        int head = methodDescriptor.indexOf("(");
        int tail = methodDescriptor.lastIndexOf(")");
        String params = methodDescriptor.substring(head + 1, tail);
        List<String> list = new ArrayList<>();

        StringBuilder sb = new StringBuilder();
        boolean isL = false;
        int lLeft = 0;
        for (int i = 0; i < params.length(); i++) {
            char c = params.charAt(i);
            if (c == '[') {
                sb.append("[]");
            } else if (c == 'L') {
                isL = true;
                lLeft = i;
            } else if (c == ';') {
                String name = params.substring(lLeft, i + 1);
                list.add(getReturnType(name) + sb);
                sb.delete(0, sb.length());
                isL = false;
            } else if (!isL) {
                list.add(getClassName(String.valueOf(c)) + sb);
                sb.delete(0, sb.length());
            }
        }
        return list;
    }

    protected String getReturnType(String methodSignature) {
        int i = methodSignature.lastIndexOf(")");
        String substring = methodSignature.substring(i + 1);
        return getClassName(substring);
    }

    protected String getClassName(String name) {
        if (BASE_TYPE_MAP.containsKey(name)) {
            return BASE_TYPE_MAP.get(name);
        }
        int arraySize = 0;
        StringBuilder array = new StringBuilder();
        while (arraySize < name.length() && name.charAt(arraySize) == '[') {
            arraySize++;
            array.append("[]");
        }
        if (arraySize != 0) {
            return getClassName(name.substring(arraySize)) + array;
        } else {
            if (!name.isEmpty() && name.charAt(0) == 'L') {
                return name.substring(1, name.length() - 1).replaceAll("[$/]", ".");
            }
        }
        return name;
    }

    public static void main(String[] args) throws IOException {
        SimpleClassVisitor simpleClassVisitor = new SimpleClassVisitor();
        ClassReader cr = new ClassReader(new FileInputStream("F:\\work\\mc\\LuaInMinecraftBukkitII-LLS-Generator\\build\\classes\\java\\main\\org\\eu\\smileyik\\lls\\SimpleClassVisitor.class"));
        cr.accept(simpleClassVisitor, 0);
    }

    public <E extends TestClass, T> E a(String b, E a) {
        return a;
    }

    public static interface A {

    }

    public static interface B {

    }

    public static interface C extends A, B {}

    public static class TestClass implements A {
        public String name;

        public String getName() {
            return name;
        }
    }
}
