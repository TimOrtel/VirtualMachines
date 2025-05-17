package tum.i2.cma

import tum.i2.common.VirtualMachine

class CMa(
    private val instructions: List<CMaInstruction>
) : VirtualMachine {

    private companion object {
        private const val MEMORY_SIZE = 200
    }

    val mem = IntArray(MEMORY_SIZE)

    var pc = 0
    var sp = -1
    var np = MEMORY_SIZE - 1

    var ep = -1
    var fp = -1

    override fun step(): Int? {
        val ir = instructions[pc]
        pc++

        return execute(ir)
    }

    override fun run(): Int {
        while (true) {
            val value = step()
            if (value != null) {
                return value
            }
        }
    }

    fun execute(instruction: CMaInstruction): Int? {
        when (val type = instruction.type) {
            CMaMiscInstruction.LOADC -> {
                loadC(instruction.firstArg)
            }

            CMaMiscInstruction.LOAD -> {
                load(instruction.firstArg)
            }

            CMaMiscInstruction.STORE -> {
                store(instruction.firstArg)
            }

            CMaMiscInstruction.LOADA -> {
                loadC(instruction.firstArg)
                load(1)
            }

            CMaMiscInstruction.STOREA -> {
                loadC(instruction.firstArg)
                store(1)
            }

            CMaMiscInstruction.POP -> {
                if (instruction.args.size == 1) {
                    sp -= instruction.firstArg
                } else {
                    stackPop()
                }
            }

            CMaMiscInstruction.JUMP -> {
                jump(instruction.firstArg)
            }

            CMaMiscInstruction.JUMPZ -> {
                jumpz(instruction.firstArg)
            }

            CMaMiscInstruction.JUMPI -> {
                jumpi(instruction.firstArg)
            }

            CMaMiscInstruction.DUP -> {
                dup()
            }

            CMaMiscInstruction.ALLOC -> {
                stackAlloc(instruction.firstArg)
            }

            CMaMiscInstruction.NEW -> {
                new()
            }

            is CmaBinaryInstruction -> {
                binary(type)
            }

            is CmaUnaryInstruction -> {
                unary(type)
            }

            CMaMiscInstruction.MARK -> {
                mark()
            }

            CMaMiscInstruction.CALL -> {
                call()
            }

            CMaMiscInstruction.ENTER -> {
                enter(instruction.firstArg)
            }

            CMaMiscInstruction.SLIDE -> {
                slide(instruction.firstArg)
            }

            CMaMiscInstruction.RETURN -> {
                `return`()
            }

            CMaMiscInstruction.LOADR -> {
                loadrc(instruction.firstArg)
                load(1)
            }

            CMaMiscInstruction.STORER -> {
                loadrc(instruction.firstArg)
                store(1)
            }

            CMaMiscInstruction.LOADRC -> {
                loadrc(instruction.firstArg)
            }

            CMaMiscInstruction.HALT -> {
                return mem[0]
            }
        }

        return null
    }

    private fun loadC(arg: Int) {
        stackPush(arg)
    }

    private fun loadrc(arg: Int) {
        stackPush(arg + fp)
    }

    private fun load(num: Int) {
        val address = stackPeek()
        for (i in 0 until num) {
            mem[sp + i] = mem[address + i]
        }
        sp += num - 1
    }

    private fun store(num: Int) {
        val address = stackPeek()

        for (i in 0 until num) {
            mem[address + i] = mem[sp - num + i]
        }

        stackPop()
    }

    private fun dup() {
        val value = stackPeek()
        stackPush(value)
    }

    private fun binary(type: CmaBinaryInstruction) {
        val second = stackPop()
        val first = stackPeek()
        stackHeadReplace(type.op(first, second))
    }

    private fun unary(type: CmaUnaryInstruction) {
        val value = stackPeek()
        stackHeadReplace(type.op(value))
    }

    private fun jump(arg: Int) {
        pc = arg
    }

    private fun jumpz(arg: Int) {
        if (stackPop() == 0) {
            jump(arg)
        }
    }

    private fun jumpi(arg: Int) {
        val offset = stackPop()
        jump(arg + offset)
    }

    private fun stackPush(value: Int) {
        sp++
        mem[sp] = value
    }

    private fun stackHeadReplace(value: Int) {
        mem[sp] = value
    }

    private fun stackPop(): Int {
        val value = mem[sp]
        sp--
        return value
    }

    private fun stackPeek(): Int {
        return mem[sp]
    }

    private fun stackAlloc(size: Int) {
        sp += size
    }

    // HEAP

    private fun new() {
        val size = stackPeek()
        np -= size
        stackHeadReplace(np)
    }

    // Function calling

    private fun mark() {
        stackPush(ep)
        stackPush(fp)
    }

    private fun call() {
        val address = stackPeek()
        stackHeadReplace(pc)
        fp = sp
        pc = address
    }

    private fun slide(m: Int) {
        // Sliding with 0 does nothing
        if (m == 0) return
        val head = stackPop()
        sp -= m
        stackHeadReplace(head)
    }

    private fun enter(q: Int) {
        ep = sp + q
        if (ep >= np) throw RuntimeException("Stack overflow")
    }

    private fun `return`() {
        pc = mem[fp]
        ep = mem[fp - 2]
        if (ep >= np) throw RuntimeException("Stack overflow")
        sp = fp - 3
        fp = mem[sp + 2]
    }
}
