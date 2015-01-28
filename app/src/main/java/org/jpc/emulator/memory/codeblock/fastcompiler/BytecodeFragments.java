/*
    JPC: An x86 PC Hardware Emulator for a pure Java Virtual Machine
    Release Version 2.4

    A project from the Physics Dept, The University of Oxford

    Copyright (C) 2007-2010 The University of Oxford

    This program is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License version 2 as published by
    the Free Software Foundation.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License along
    with this program; if not, write to the Free Software Foundation, Inc.,
    51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 
    Details (including contact information) can be found at: 

    jpc.sourceforge.net
    or the developer website
    sourceforge.net/projects/jpc/

    Conceived and Developed by:
    Rhys Newman, Ian Preston, Chris Dennis

    End of licence header
*/

package org.jpc.emulator.memory.codeblock.fastcompiler;

import android.support.annotation.NonNull;

import org.jpc.emulator.processor.Processor;

import static org.jpc.classfile.JavaOpcode.ALOAD_1;
import static org.jpc.classfile.JavaOpcode.DUP;
import static org.jpc.classfile.JavaOpcode.DUP_X2;
import static org.jpc.classfile.JavaOpcode.GETFIELD;
import static org.jpc.classfile.JavaOpcode.ICONST_0;
import static org.jpc.classfile.JavaOpcode.INVOKEVIRTUAL;
import static org.jpc.classfile.JavaOpcode.POP;
import static org.jpc.classfile.JavaOpcode.PUTFIELD;
import static org.jpc.classfile.JavaOpcode.SWAP;
import static org.jpc.emulator.memory.codeblock.fastcompiler.FASTCompiler.ELEMENT_COUNT;
import static org.jpc.emulator.memory.codeblock.fastcompiler.FASTCompiler.PROCESSOR_ELEMENT_ACFLAG;
import static org.jpc.emulator.memory.codeblock.fastcompiler.FASTCompiler.PROCESSOR_ELEMENT_ADDR0;
import static org.jpc.emulator.memory.codeblock.fastcompiler.FASTCompiler.PROCESSOR_ELEMENT_AFLAG;
import static org.jpc.emulator.memory.codeblock.fastcompiler.FASTCompiler.PROCESSOR_ELEMENT_CFLAG;
import static org.jpc.emulator.memory.codeblock.fastcompiler.FASTCompiler.PROCESSOR_ELEMENT_COUNT;
import static org.jpc.emulator.memory.codeblock.fastcompiler.FASTCompiler.PROCESSOR_ELEMENT_CPL;
import static org.jpc.emulator.memory.codeblock.fastcompiler.FASTCompiler.PROCESSOR_ELEMENT_CPU;
import static org.jpc.emulator.memory.codeblock.fastcompiler.FASTCompiler.PROCESSOR_ELEMENT_CS;
import static org.jpc.emulator.memory.codeblock.fastcompiler.FASTCompiler.PROCESSOR_ELEMENT_DFLAG;
import static org.jpc.emulator.memory.codeblock.fastcompiler.FASTCompiler.PROCESSOR_ELEMENT_DS;
import static org.jpc.emulator.memory.codeblock.fastcompiler.FASTCompiler.PROCESSOR_ELEMENT_EAX;
import static org.jpc.emulator.memory.codeblock.fastcompiler.FASTCompiler.PROCESSOR_ELEMENT_EBP;
import static org.jpc.emulator.memory.codeblock.fastcompiler.FASTCompiler.PROCESSOR_ELEMENT_EBX;
import static org.jpc.emulator.memory.codeblock.fastcompiler.FASTCompiler.PROCESSOR_ELEMENT_ECX;
import static org.jpc.emulator.memory.codeblock.fastcompiler.FASTCompiler.PROCESSOR_ELEMENT_EDI;
import static org.jpc.emulator.memory.codeblock.fastcompiler.FASTCompiler.PROCESSOR_ELEMENT_EDX;
import static org.jpc.emulator.memory.codeblock.fastcompiler.FASTCompiler.PROCESSOR_ELEMENT_EIP;
import static org.jpc.emulator.memory.codeblock.fastcompiler.FASTCompiler.PROCESSOR_ELEMENT_ES;
import static org.jpc.emulator.memory.codeblock.fastcompiler.FASTCompiler.PROCESSOR_ELEMENT_ESI;
import static org.jpc.emulator.memory.codeblock.fastcompiler.FASTCompiler.PROCESSOR_ELEMENT_ESP;
import static org.jpc.emulator.memory.codeblock.fastcompiler.FASTCompiler.PROCESSOR_ELEMENT_FS;
import static org.jpc.emulator.memory.codeblock.fastcompiler.FASTCompiler.PROCESSOR_ELEMENT_GDTR;
import static org.jpc.emulator.memory.codeblock.fastcompiler.FASTCompiler.PROCESSOR_ELEMENT_GS;
import static org.jpc.emulator.memory.codeblock.fastcompiler.FASTCompiler.PROCESSOR_ELEMENT_IDFLAG;
import static org.jpc.emulator.memory.codeblock.fastcompiler.FASTCompiler.PROCESSOR_ELEMENT_IDTR;
import static org.jpc.emulator.memory.codeblock.fastcompiler.FASTCompiler.PROCESSOR_ELEMENT_IFLAG;
import static org.jpc.emulator.memory.codeblock.fastcompiler.FASTCompiler.PROCESSOR_ELEMENT_IOPL;
import static org.jpc.emulator.memory.codeblock.fastcompiler.FASTCompiler.PROCESSOR_ELEMENT_IOPORTS;
import static org.jpc.emulator.memory.codeblock.fastcompiler.FASTCompiler.PROCESSOR_ELEMENT_LDTR;
import static org.jpc.emulator.memory.codeblock.fastcompiler.FASTCompiler.PROCESSOR_ELEMENT_NTFLAG;
import static org.jpc.emulator.memory.codeblock.fastcompiler.FASTCompiler.PROCESSOR_ELEMENT_OFLAG;
import static org.jpc.emulator.memory.codeblock.fastcompiler.FASTCompiler.PROCESSOR_ELEMENT_PFLAG;
import static org.jpc.emulator.memory.codeblock.fastcompiler.FASTCompiler.PROCESSOR_ELEMENT_RFLAG;
import static org.jpc.emulator.memory.codeblock.fastcompiler.FASTCompiler.PROCESSOR_ELEMENT_SFLAG;
import static org.jpc.emulator.memory.codeblock.fastcompiler.FASTCompiler.PROCESSOR_ELEMENT_SS;
import static org.jpc.emulator.memory.codeblock.fastcompiler.FASTCompiler.PROCESSOR_ELEMENT_TFLAG;
import static org.jpc.emulator.memory.codeblock.fastcompiler.FASTCompiler.PROCESSOR_ELEMENT_TSS;
import static org.jpc.emulator.memory.codeblock.fastcompiler.FASTCompiler.PROCESSOR_ELEMENT_VIFLAG;
import static org.jpc.emulator.memory.codeblock.fastcompiler.FASTCompiler.PROCESSOR_ELEMENT_VIPFLAG;
import static org.jpc.emulator.memory.codeblock.fastcompiler.FASTCompiler.PROCESSOR_ELEMENT_VMFLAG;
import static org.jpc.emulator.memory.codeblock.fastcompiler.FASTCompiler.PROCESSOR_ELEMENT_ZFLAG;

/**
 * Provides bytecode fragments that load and store values from the
 * <code>Processor</code> instance.  Fragments are <code>Object</code> arrays
 * containing either <code>Integer</code> objects for bytecode values, or object
 * placeholders for immediate values and the length of the block.
 * @author Chris Dennis
 */
public abstract class BytecodeFragments
{
    protected static final Object IMMEDIATE = new Object();
    protected static final Object X86LENGTH = new Object();
    private static final Object[][] pushCodeArray = new Object[ELEMENT_COUNT][];
    private static final Object[][] popCodeArray = new Object[PROCESSOR_ELEMENT_COUNT][];

    static {
        pushCodeArray[PROCESSOR_ELEMENT_EAX] = new Object[]{(int) ALOAD_1,
                (int) GETFIELD, field("eax")
        };
        pushCodeArray[PROCESSOR_ELEMENT_ECX] = new Object[]{(int) ALOAD_1,
                (int) GETFIELD, field("ecx")
        };
        pushCodeArray[PROCESSOR_ELEMENT_EDX] = new Object[]{(int) ALOAD_1,
                (int) GETFIELD, field("edx")
        };
        pushCodeArray[PROCESSOR_ELEMENT_EBX] = new Object[]{(int) ALOAD_1,
                (int) GETFIELD, field("ebx")
        };
        pushCodeArray[PROCESSOR_ELEMENT_ESP] = new Object[]{(int) ALOAD_1,
                (int) GETFIELD, field("esp")
        };
        pushCodeArray[PROCESSOR_ELEMENT_EBP] = new Object[]{(int) ALOAD_1,
                (int) GETFIELD, field("ebp")
        };
        pushCodeArray[PROCESSOR_ELEMENT_ESI] = new Object[]{(int) ALOAD_1,
                (int) GETFIELD, field("esi")
        };
        pushCodeArray[PROCESSOR_ELEMENT_EDI] = new Object[]{(int) ALOAD_1,
                (int) GETFIELD, field("edi")
        };
        
        pushCodeArray[PROCESSOR_ELEMENT_EIP] = new Object[]{(int) ALOAD_1,
                (int) GETFIELD, field("eip")
        };

        pushCodeArray[PROCESSOR_ELEMENT_CFLAG] = new Object[]{(int) ALOAD_1,
                (int) INVOKEVIRTUAL, method("getCarryFlag")
        };
        pushCodeArray[PROCESSOR_ELEMENT_PFLAG] = new Object[]{(int) ALOAD_1,
                (int) INVOKEVIRTUAL, method("getParityFlag")
        };
        pushCodeArray[PROCESSOR_ELEMENT_AFLAG] = new Object[]{(int) ALOAD_1,
                (int) INVOKEVIRTUAL, method("getAuxiliaryCarryFlag")
        };
        pushCodeArray[PROCESSOR_ELEMENT_ZFLAG] = new Object[]{(int) ALOAD_1,
                (int) INVOKEVIRTUAL, method("getZeroFlag")
        };
        pushCodeArray[PROCESSOR_ELEMENT_SFLAG] = new Object[]{(int) ALOAD_1,
                (int) INVOKEVIRTUAL, method("getSignFlag")
        };
        pushCodeArray[PROCESSOR_ELEMENT_TFLAG] = new Object[]{(int) ALOAD_1,
                (int) GETFIELD, field("eflagsTrap")
        };
        pushCodeArray[PROCESSOR_ELEMENT_IFLAG] = new Object[]{(int) ALOAD_1,
                (int) GETFIELD, field("eflagsInterruptEnable")
        };
        pushCodeArray[PROCESSOR_ELEMENT_DFLAG] = new Object[]{(int) ALOAD_1,
                (int) GETFIELD, field("eflagsDirection")
        };
        pushCodeArray[PROCESSOR_ELEMENT_OFLAG] = new Object[]{(int) ALOAD_1,
                (int) INVOKEVIRTUAL, method("getOverflowFlag")
        };
        pushCodeArray[PROCESSOR_ELEMENT_IOPL] = new Object[]{(int) ALOAD_1,
                (int) GETFIELD, field("eflagsIOPrivilegeLevel")
        };
        pushCodeArray[PROCESSOR_ELEMENT_NTFLAG] = new Object[]{(int) ALOAD_1,
                (int) GETFIELD, field("eflagsNestedTask")
        };
        pushCodeArray[PROCESSOR_ELEMENT_RFLAG] = new Object[]{(int) ALOAD_1,
                (int) GETFIELD, field("eflagsResume")
        };
        pushCodeArray[PROCESSOR_ELEMENT_VMFLAG] = new Object[]{(int) ALOAD_1,
                (int) GETFIELD, field("eflagsVirtual8086Mode")
        };
        pushCodeArray[PROCESSOR_ELEMENT_ACFLAG] = new Object[]{(int) ALOAD_1,
                (int) GETFIELD, field("eflagsAlignmentCheck")
        };
        pushCodeArray[PROCESSOR_ELEMENT_VIFLAG] = new Object[]{(int) ALOAD_1,
                (int) GETFIELD, field("eflagsVirtualInterrupt")
        };
        pushCodeArray[PROCESSOR_ELEMENT_VIPFLAG] = new Object[]{(int) ALOAD_1,
                (int) GETFIELD, field("eflagsVirtualInterruptPending")
        };
        pushCodeArray[PROCESSOR_ELEMENT_IDFLAG] = new Object[]{(int) ALOAD_1,
                (int) GETFIELD, field("eflagsID")
        };
	
        pushCodeArray[PROCESSOR_ELEMENT_ES] = new Object[]{(int) ALOAD_1,
                (int) GETFIELD, field("es")
        };
        pushCodeArray[PROCESSOR_ELEMENT_CS] = new Object[]{(int) ALOAD_1,
                (int) GETFIELD, field("cs")
        };
        pushCodeArray[PROCESSOR_ELEMENT_SS] = new Object[]{(int) ALOAD_1,
                (int) GETFIELD, field("ss")
        };
        pushCodeArray[PROCESSOR_ELEMENT_DS] = new Object[]{(int) ALOAD_1,
                (int) GETFIELD, field("ds")
        };
        pushCodeArray[PROCESSOR_ELEMENT_FS] = new Object[]{(int) ALOAD_1,
                (int) GETFIELD, field("fs")
        };
        pushCodeArray[PROCESSOR_ELEMENT_GS] = new Object[]{(int) ALOAD_1,
                (int) GETFIELD, field("gs")
        };
        pushCodeArray[PROCESSOR_ELEMENT_IDTR] = new Object[]{(int) ALOAD_1,
                (int) GETFIELD, field("idtr")
        };
        pushCodeArray[PROCESSOR_ELEMENT_GDTR] = new Object[]{(int) ALOAD_1,
                (int) GETFIELD, field("gdtr")
        };
        pushCodeArray[PROCESSOR_ELEMENT_LDTR] = new Object[]{(int) ALOAD_1,
                (int) GETFIELD, field("ldtr")
        };
        pushCodeArray[PROCESSOR_ELEMENT_TSS] = new Object[]{(int) ALOAD_1,
                (int) GETFIELD, field("tss")
        };

        pushCodeArray[PROCESSOR_ELEMENT_CPL] = new Object[]{(int) ALOAD_1,
                (int) INVOKEVIRTUAL, method("getCPL")
        };

        pushCodeArray[PROCESSOR_ELEMENT_IOPORTS] = new Object[]{(int) ALOAD_1,
                (int) GETFIELD, field("ioports")
        };

        pushCodeArray[PROCESSOR_ELEMENT_CPU] = new Object[]{(int) ALOAD_1};

        pushCodeArray[PROCESSOR_ELEMENT_ADDR0] = new Object[]{(int) ICONST_0};
    }

    static {

        popCodeArray[PROCESSOR_ELEMENT_EAX] = new Object[]{(int) ALOAD_1,
                (int) SWAP,
                (int) PUTFIELD, field("eax")
        };
        popCodeArray[PROCESSOR_ELEMENT_ECX] = new Object[]{(int) ALOAD_1,
                (int) SWAP,
                (int) PUTFIELD, field("ecx")
        };
        popCodeArray[PROCESSOR_ELEMENT_EDX] = new Object[]{(int) ALOAD_1,
                (int) SWAP,
                (int) PUTFIELD, field("edx")
        };
        popCodeArray[PROCESSOR_ELEMENT_EBX] = new Object[]{(int) ALOAD_1,
                (int) SWAP,
                (int) PUTFIELD, field("ebx")
        };
        popCodeArray[PROCESSOR_ELEMENT_ESP] = new Object[]{(int) ALOAD_1,
                (int) SWAP,
                (int) PUTFIELD, field("esp")
        };
        popCodeArray[PROCESSOR_ELEMENT_EBP] = new Object[]{(int) ALOAD_1,
                (int) SWAP,
                (int) PUTFIELD, field("ebp")
        };
        popCodeArray[PROCESSOR_ELEMENT_ESI] = new Object[]{(int) ALOAD_1,
                (int) SWAP,
                (int) PUTFIELD, field("esi")
        };
        popCodeArray[PROCESSOR_ELEMENT_EDI] = new Object[]{(int) ALOAD_1,
                (int) SWAP,
                (int) PUTFIELD, field("edi")
        };

        popCodeArray[PROCESSOR_ELEMENT_EIP] = new Object[]{(int) ALOAD_1,
                (int) SWAP,
                (int) PUTFIELD, field("eip")
        };

        popCodeArray[PROCESSOR_ELEMENT_CFLAG] = new Object[]{(int) ALOAD_1,
                (int) SWAP,
                (int) INVOKEVIRTUAL, method("setCarryFlag", Boolean.TYPE)
        };

        popCodeArray[PROCESSOR_ELEMENT_PFLAG] = new Object[]{(int) ALOAD_1,
                (int) SWAP,
                (int) INVOKEVIRTUAL,
                                                             method("setParityFlag", Boolean.TYPE)
        };
        popCodeArray[PROCESSOR_ELEMENT_AFLAG] = new Object[]{(int) ALOAD_1,
                (int) SWAP,
                (int) INVOKEVIRTUAL,
                                                             method("setAuxiliaryCarryFlag", Boolean.TYPE)
        };
        popCodeArray[PROCESSOR_ELEMENT_ZFLAG] = new Object[]{(int) ALOAD_1,
                (int) SWAP,
                (int) INVOKEVIRTUAL, method("setZeroFlag", Boolean.TYPE)
        };
        popCodeArray[PROCESSOR_ELEMENT_SFLAG] = new Object[]{(int) ALOAD_1,
                (int) SWAP,
                (int) INVOKEVIRTUAL, method("setSignFlag", Boolean.TYPE)
        };
        popCodeArray[PROCESSOR_ELEMENT_TFLAG] = new Object[]{(int) ALOAD_1,
                (int) SWAP,
                (int) PUTFIELD, field("eflagsTrap")
        };
        popCodeArray[PROCESSOR_ELEMENT_IFLAG] = new Object[]{(int) DUP,
                (int) ALOAD_1,
                (int) DUP_X2,
                (int) SWAP,
                (int) PUTFIELD, field("eflagsInterruptEnable"),
                (int) PUTFIELD, field("eflagsInterruptEnableSoon")
        };
        popCodeArray[PROCESSOR_ELEMENT_DFLAG] = new Object[]{(int) ALOAD_1,
                (int) SWAP,
                (int) PUTFIELD, field("eflagsDirection")
        };
        popCodeArray[PROCESSOR_ELEMENT_OFLAG] = new Object[]{(int) ALOAD_1,
                (int) SWAP,
                (int) INVOKEVIRTUAL,
                                                             method("setOverflowFlag", Boolean.TYPE)
        };
        popCodeArray[PROCESSOR_ELEMENT_IOPL] = new Object[]{(int) ALOAD_1,
                (int) SWAP,
                (int) PUTFIELD, field("eflagsIOPrivilegeLevel")
        };
        popCodeArray[PROCESSOR_ELEMENT_NTFLAG] = new Object[]{(int) ALOAD_1,
                (int) SWAP,
                (int) PUTFIELD, field("eflagsNestedTask")
        };
        popCodeArray[PROCESSOR_ELEMENT_RFLAG] = new Object[]{(int) ALOAD_1,
                (int) SWAP,
                (int) PUTFIELD, field("eflagsResume")
        };
        popCodeArray[PROCESSOR_ELEMENT_VMFLAG] = new Object[]{(int) ALOAD_1,
                (int) SWAP,
                (int) PUTFIELD, field("eflagsVirtual8086Mode")
        };
        popCodeArray[PROCESSOR_ELEMENT_ACFLAG] = new Object[]{(int) ALOAD_1,
                (int) SWAP,
                (int) PUTFIELD, field("eflagsAlignmentCheck")
        };
        popCodeArray[PROCESSOR_ELEMENT_VIFLAG] = new Object[]{(int) ALOAD_1,
                (int) SWAP,
                (int) PUTFIELD, field("eflagsVirtualInterrupt")
        };
        popCodeArray[PROCESSOR_ELEMENT_VIPFLAG] = new Object[]{(int) ALOAD_1,
                (int) SWAP,
                (int) PUTFIELD, field("eflagsVirtualInterruptPending")
        };
        popCodeArray[PROCESSOR_ELEMENT_IDFLAG] = new Object[]{(int) ALOAD_1,
                (int) SWAP,
                (int) PUTFIELD, field("eflagsID")
        };

        popCodeArray[PROCESSOR_ELEMENT_ES] = new Object[]{(int) ALOAD_1,
                (int) SWAP,
                (int) PUTFIELD, field("es")
        };
        popCodeArray[PROCESSOR_ELEMENT_CS] = new Object[]{(int) ALOAD_1,
                (int) SWAP,
                (int) PUTFIELD, field("cs")
        };
        popCodeArray[PROCESSOR_ELEMENT_SS] = new Object[]{(int) ALOAD_1,
                (int) SWAP,
                (int) PUTFIELD, field("ss")
        };
        popCodeArray[PROCESSOR_ELEMENT_DS] = new Object[]{(int) ALOAD_1,
                (int) SWAP,
                (int) PUTFIELD, field("ds")
        };
        popCodeArray[PROCESSOR_ELEMENT_FS] = new Object[]{(int) ALOAD_1,
                (int) SWAP,
                (int) PUTFIELD, field("fs")
        };
        popCodeArray[PROCESSOR_ELEMENT_GS] = new Object[]{(int) ALOAD_1,
                (int) SWAP,
                (int) PUTFIELD, field("gs")
        };
        popCodeArray[PROCESSOR_ELEMENT_IDTR] = new Object[]{(int) ALOAD_1,
                (int) SWAP,
                (int) PUTFIELD, field("idtr")
        };
        popCodeArray[PROCESSOR_ELEMENT_GDTR] = new Object[]{(int) ALOAD_1,
                (int) SWAP,
                (int) PUTFIELD, field("gdtr")
        };
        popCodeArray[PROCESSOR_ELEMENT_LDTR] = new Object[]{(int) ALOAD_1,
                (int) SWAP,
                (int) PUTFIELD, field("ldtr")
        };
        popCodeArray[PROCESSOR_ELEMENT_TSS] = new Object[]{(int) ALOAD_1,
                (int) SWAP,
                (int) PUTFIELD, field("tss")
        };

        popCodeArray[PROCESSOR_ELEMENT_CPU] = new Object[]{(int) POP};
        popCodeArray[PROCESSOR_ELEMENT_ADDR0] = new Object[]{(int) POP};
    }

    /**
     * Returns bytecode fragment for pushing the given element onto the stack.
     * @param element index of processor element
     * @return bytecode fragment array
     */
    public static Object[] pushCode(int element)
    {
        Object[] temp = pushCodeArray[element];
        if (temp == null)
            throw new IllegalStateException("Non existant CPU Element: " + element);
        return temp;
    }

    /**
     * Returns bytecode fragment for poping the given element from the stack.
     * @param element index of processor element
     * @return bytecode fragment array
     */
    public static Object[] popCode(int element)
    {
        Object[] temp = popCodeArray[element];
        if (temp == null) 
            throw new IllegalStateException("Non existent CPU Element: " + element);
        return temp;
    }

    @NonNull
    private static ConstantPoolSymbol field(String name)
    {
        return field(Processor.class, name);
    }

    @NonNull
    private static ConstantPoolSymbol field(@NonNull Class cls, String name)
    {
        try {
            return new ConstantPoolSymbol(cls.getDeclaredField(name));
        } catch (NoSuchFieldException e) {
            throw new IllegalStateException(e);
        }
    }

    @NonNull
    private static ConstantPoolSymbol method(String name)
    {
        return method(name, new Class[0]);
    }

    @NonNull
    private static ConstantPoolSymbol method(String name, Class arg)
    {
        return method(name, new Class[]{arg});
    }

    @NonNull
    private static ConstantPoolSymbol method(String name, Class[] args)
    {
        return method(Processor.class, name, args);
    }

    @NonNull
    private static ConstantPoolSymbol method(@NonNull Class cls, String name, Class[] args)
    {
        try {
            return new ConstantPoolSymbol(cls.getMethod(name, args));
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException(e);
        }
    }

    @NonNull
    protected static ConstantPoolSymbol integer(int value)
    {
        return new ConstantPoolSymbol(value);
    }

    @NonNull
    protected static ConstantPoolSymbol longint(long value)
    {
	return new ConstantPoolSymbol(value);
    }

    protected BytecodeFragments()
    {
    }
}


