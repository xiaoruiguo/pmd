/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */


package net.sourceforge.pmd.lang.java.symbols;

import java.lang.reflect.Modifier;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.NotImplementedException;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import net.sourceforge.pmd.lang.java.ast.ASTAnyTypeDeclaration;


/**
 * Abstraction over a {@link Class} instance. This is not a type, it's
 * the *declaration* of a type. For example, a class symbol representing
 * a generic class can provide access to the formal type parameters, but
 * the symbol does not represent a specific parametrization of a type.
 *
 * <p>Class symbols represent the full range of types represented by {@link Class}:
 * classes, interfaces, arrays, and primitives. This excludes type variables,
 * intersection types, parameterized types, wildcard types, etc., which are only
 * compile-time constructs.
 *
 * @since 7.0.0
 */
public interface JClassSymbol extends JTypeDeclSymbol,
                                      JTypeParameterOwnerSymbol,
                                      BoundToNode<ASTAnyTypeDeclaration> {

    /**
     * Returns the binary name of this type, as specified by the JLS:
     * <a href="https://docs.oracle.com/javase/specs/jls/se7/html/jls-13.html#jls-13.1">the JLS</a>.
     * For array types this returns the binary name of the component followed by "[]".
     */
    @NonNull
    String getBinaryName();


    /**
     * Returns the simple name of this class, as specified by
     * {@link Class#getCanonicalName()}.
     */
    @Nullable
    String getCanonicalName();


    /**
     * Returns true if this class is a symbolic reference to an unresolved
     * class. In that case no information about the symbol are known except
     * its name, and the accessors of this class return default values.
     *
     * <p>This kind of symbol is introduced to allow for some best-effort
     * symbolic resolution. For example in:
     * <pre>{@code
     * import org.Bar;
     *
     * Bar foo = new Bar();
     * }</pre>
     * and supposing {@code org.Bar} is not on the classpath. The type
     * of {@code foo} is {@code Bar}, which we can qualify to {@code org.Bar} thanks to the
     * import (via symbol tables, and without even querying the classpath).
     * Even though we don't know what members {@code org.Bar} has, a
     * test for {@code typeIs("org.Bar")} would succeed with certainty,
     * so it makes sense to preserve the name information and not give
     * up too early.
     *
     * <p>Note that unresolved types are always created from an unresolved
     * <i>canonical name</i>, so they can't be just <i>any</i> type. For example,
     * they can't be array types, nor local classes (since those are lexically
     * scoped, so always resolvable), nor anonymous classes (can only be referenced
     * on their declaration site), etc.
     */
    boolean isUnresolved();


    /**
     * Returns the method or constructor this symbol is declared in, if
     * it represents a {@linkplain #isLocalClass() local class declaration}.
     *
     * <p>Notice, that this returns null also if this class is local to
     * a class or instance initializer.
     */
    @Nullable
    default JExecutableSymbol getEnclosingMethod() {
        throw new NotImplementedException("TODO, trickier than it appears");
    }


    @Override
    default JTypeParameterOwnerSymbol getEnclosingTypeParameterOwner() {
        JExecutableSymbol enclosingMethod = getEnclosingMethod();
        return enclosingMethod != null ? enclosingMethod : getEnclosingClass();
    }


    /**
     * Returns the member classes declared directly in this class.
     *
     * @see Class#getDeclaredClasses()
     */
    List<JClassSymbol> getDeclaredClasses();


    /** Returns a field with the given name accessed defined in this class. */
    @Nullable
    default JClassSymbol getDeclaredClass(String name) {
        for (JClassSymbol klass : getDeclaredClasses()) {
            if (klass.getSimpleName().equals(name)) {
                return klass;
            }
        }
        return null;
    }


    /**
     * Returns the methods declared directly in this class.
     * <i>This excludes bridges and other synthetic methods.</i>
     *
     * <p>For an array type T[], to the difference of {@link Class},
     * this method returns a one-element list with the {@link Cloneable#clone()}
     * method, as if declared like so: {@code public final T[] clone() {...}}.
     *
     * @see Class#getDeclaredMethods()
     */
    List<JMethodSymbol> getDeclaredMethods();


    /**
     * Returns the constructors declared by this class.
     * <i>This excludes synthetic constructors.</i>
     *
     * <p>For an array type T[], and to the difference of {@link Class},
     * this should return a one-element list with a constructor
     * having the same modifiers as the array type, and a single
     * {@code int} parameter.
     *
     * @see Class#getDeclaredConstructors()
     */
    List<JConstructorSymbol> getConstructors();


    /**
     * Returns the fields declared directly in this class.
     * <i>This excludes synthetic fields.</i>
     *
     * <p>For arrays, and to the difference of {@link Class},
     * this should return a one-element list with the
     * {@code public final int length} field.
     *
     * @see Class#getDeclaredFields()
     */
    List<JFieldSymbol> getDeclaredFields();


    /** Returns a field with the given name accessed defined in this class. */
    @Nullable
    default JFieldSymbol getDeclaredField(String name) {
        for (JFieldSymbol field : getDeclaredFields()) {
            if (field.getSimpleName().equals(name)) {
                return field;
            }
        }
        return null;
    }


    /** Returns all methods with the given name declared in this class. */
    default List<JMethodSymbol> getDeclaredMethods(String name) {
        return getDeclaredMethods().stream().filter(it -> it.getSimpleName().equals(name)).collect(Collectors.toList());
    }


    /**
     * Returns the superclass symbol if it exists. Returns null if this
     * class represents an interface or the class {@link Object}.
     */
    @Nullable
    JClassSymbol getSuperclass();


    /** Returns the direct super-interfaces of this class or interface symbol. */
    List<JClassSymbol> getSuperInterfaces();


    default boolean isAbstract() {
        return Modifier.isAbstract(getModifiers());
    }


    /** Returns the component symbol, returns null if this is not an array. */
    @Nullable
    JTypeDeclSymbol getArrayComponent();


    boolean isArray();

    boolean isPrimitive();

    boolean isInterface();

    boolean isEnum();

    boolean isAnnotation();

    boolean isLocalClass();

    boolean isAnonymousClass();


    default boolean isClass() {
        return !isInterface() && !isArray() && !isPrimitive();
    }


    @Override
    default <R, P> R acceptVisitor(SymbolVisitor<R, P> visitor, P param) {
        return visitor.visitClass(this, param);
    }
}