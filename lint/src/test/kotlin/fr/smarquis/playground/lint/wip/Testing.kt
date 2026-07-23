package fr.smarquis.playground.lint.wip


interface Foo
interface Printable
open class A
class B: A(), Printable

fun safe(a: A, b: B) {
    a as A // identity
    b as A // upcast
    b as Printable // interface
}

fun nullsafe(a: A, b: B) {
    a as? A // identity
    a as? B // downcast
    b as? A // upcast
    a as? Printable // interface (through downcast)
    b as? Printable // interface
}
