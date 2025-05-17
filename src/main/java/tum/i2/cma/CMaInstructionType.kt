package tum.i2.cma

import tum.i2.cma.CMaMiscInstruction.*

import java.util.*

sealed interface CMaInstructionType {

    val name: String

    companion object {
        //
        private val STRING_TO_ENUM: MutableMap<String?, CMaInstructionType> = HashMap<String?, CMaInstructionType>()

        private val entries: List<CMaInstructionType> = CmaArithmeticInstruction.entries +
                CmaComparisonInstruction.entries +
                CmaUnaryInstruction.entries +
                CMaMiscInstruction.entries

        init {
            for (type in entries) {
                STRING_TO_ENUM.put(type.name, type)
            }
        }

        @JvmStatic
        fun fromString(name: String): CMaInstructionType {
            val type: CMaInstructionType = STRING_TO_ENUM[name.uppercase(Locale.getDefault())]!!
            requireNotNull(type) { "Unknown instruction type: $name" }
            return type
        }

        @JvmStatic
        fun expectedNumberOfArguments(type: CMaInstructionType): Int {
            when (type) {
                LOADC, LOAD, STORE, LOADA, STOREA, JUMP, JUMPZ, JUMPI, ALLOC, LOADRC, STORER, ENTER, SLIDE, LOADR -> return 1
                else -> return 0
            }
        }
    }
}

interface CmaBinaryInstruction : CMaInstructionType {
    val op: (Int, Int) -> Int
}

enum class CmaArithmeticInstruction(override val op: (Int, Int) -> Int) : CmaBinaryInstruction {
    ADD(Int::plus),
    SUB(Int::minus),
    MUL(Int::times),
    DIV(Int::div),
    MOD(Int::mod),
    AND(Int::and),
    OR(Int::or),
    XOR(Int::xor),
}

enum class CmaComparisonInstruction(op: (Int, Int) -> Boolean) : CmaBinaryInstruction {
    EQ(Int::equals),
    NEQ({ a, b -> a != b }),
    LE({ a, b -> a < b }),
    LEQ({ a, b -> a <= b }),
    GR({ a, b -> a > b }),
    GEQ({ a, b -> a >= b });

    override val op: (Int, Int) -> Int = { a, b -> if (op(a, b)) 1 else 0 }
}

enum class CmaUnaryInstruction(val op: (Int) -> Int) : CMaInstructionType {
    NOT({ if (it == 0) 1 else 0 }),
    NEG(Int::unaryMinus)
}

enum class CMaMiscInstruction : CMaInstructionType {
    HALT,

    LOADC,

    // Assignments
    LOAD,
    STORE,
    LOADA,
    STOREA,
    LOADR,
    STORER,
    LOADRC,

    // Statements (as introduced in Statements and Statement Sequences)
    POP,

    // Conditional and Iterative Statements
    JUMP,
    JUMPZ,

    // Introduced in the Switch Statement
    JUMPI,
    DUP,

    // Introduced in Storage Allocation for Variables
    ALLOC,

    NEW,


    // Function stuff
    MARK,
    CALL,
    ENTER,
    RETURN,
    SLIDE

    ;
}
