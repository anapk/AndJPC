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

package org.jpc.emulator.memory.codeblock;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.jpc.emulator.memory.codeblock.fastcompiler.FASTCompiler;
import org.jpc.emulator.memory.codeblock.fastcompiler.prot.ProtectedModeTemplateBlock;
import org.jpc.emulator.memory.codeblock.fastcompiler.real.RealModeTemplateBlock;

import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Ian Preston
 */
public class CachedCodeBlockCompiler implements CodeBlockCompiler
{
    @Nullable
    private HashSet availableClassNames = null;
    private boolean loadedClass = false, listedClassNames = false;

    private void getAvailableClassNames()
    {
        listedClassNames = true;
            
        try
        {
            HashSet buffer = new HashSet();
            URLClassLoader cl = (URLClassLoader) getClass().getClassLoader();
            URL[] urls = cl.getURLs();

            for (URL url : urls) {
                if (!url.toString().endsWith(".jar"))
                    continue;

                try {
                    URLConnection conn = url.openConnection();
                    conn.setUseCaches(true);
                    conn.setConnectTimeout(5000);
                    conn.setReadTimeout(5000);

                    JarInputStream jin = new JarInputStream(conn.getInputStream());
                    while (true) {
                        JarEntry jen = jin.getNextJarEntry();
                        if (jen == null)
                            break;
                        String name = jen.getName().trim();
                        if (name.endsWith(".class"))
                            name = name.substring(0, name.length() - 6);
                        name = name.replace("/", ".");
                        name = name.replace("\\", ".");
                        buffer.add(name);
                    }

                    jin.close();
                } catch (Exception e) {
                    System.out.println("Warning: exception listing contents of JAR resource " + url);
                    e.printStackTrace();
                }
            }

            availableClassNames = buffer;
        }
        catch (Exception ignored) {}
    }
    
    @Nullable
    public RealModeCodeBlock getRealModeCodeBlock(@NonNull InstructionSource source)
    {
        if (!listedClassNames)
            getAvailableClassNames();

        try
        {
            int[] newMicrocodes = getMicrocodesArray(source);
            String className = "org.jpc.dynamic.FAST_RM_" + FASTCompiler.getHash(newMicrocodes);
            
            if (availableClassNames != null)
            {
                if (!availableClassNames.contains(className))
                    return null;
            }

            Class oldClass = Class.forName(className);
            int[] oldMicrocodes = ((RealModeTemplateBlock) oldClass.newInstance()).getMicrocodes();
            boolean same = true;
            if (oldMicrocodes.length != newMicrocodes.length)
                same = false;
            else
            {
                for (int i = 0; i < oldMicrocodes.length; i++)
                {
                    if (oldMicrocodes[i] != newMicrocodes[i])
                        same = false;
                }
            }

            if (same) 
            {
                if (!loadedClass)
                {
                    loadedClass = true;
                    System.out.println("Loaded Precompiled Class");
                }
                return (RealModeCodeBlock) oldClass.newInstance();
            } 
            else
                return null;
        } 
        catch (@NonNull InstantiationException | IllegalAccessException ex)
        {
            Logger.getLogger(CachedCodeBlockCompiler.class.getName()).log(Level.SEVERE, null, ex);
        } catch (VerifyError e)
        {
            e.printStackTrace();
        } 
        catch (ClassNotFoundException ignored) {}

        return null;
    }

    @Nullable
    public ProtectedModeCodeBlock getProtectedModeCodeBlock(@NonNull InstructionSource source)
    {
        if (!listedClassNames)
            getAvailableClassNames();

        try
        {
            int[] newMicrocodes = getMicrocodesArray(source);
            String className = "org.jpc.dynamic.FAST_PM_" + FASTCompiler.getHash(newMicrocodes);

            if (availableClassNames != null)
            {
                if (!availableClassNames.contains(className))
                    return null;
            }

            Class oldClass = Class.forName(className);
            int[] oldMicrocodes = ((ProtectedModeTemplateBlock) oldClass.newInstance()).getMicrocodes();
            boolean same = true;

            if (oldMicrocodes.length != newMicrocodes.length)
            {
                same = false;
            } 
            else
            {
                for (int i = 0; i < oldMicrocodes.length; i++)
                {
                    if (oldMicrocodes[i] != newMicrocodes[i])
                    {
                        same = false;
                    }
                }
            }

            if (same)
            {
                if (!loadedClass)
                {
                    loadedClass = true;
                    System.out.println("Loaded Precompiled Class");
                }
                return (ProtectedModeCodeBlock) oldClass.newInstance();
            } 
            else
                return null;
        } 
        catch (@NonNull InstantiationException | IllegalAccessException ex)
        {
            Logger.getLogger(CachedCodeBlockCompiler.class.getName()).log(Level.SEVERE, null, ex);
        } catch (VerifyError e)
        {
            e.printStackTrace();
        }
        catch (ClassNotFoundException ignored) {}

        return null;
    }

    @Nullable
    public Virtual8086ModeCodeBlock getVirtual8086ModeCodeBlock(InstructionSource source)
    {
        if (!listedClassNames)
            getAvailableClassNames();

        return null;
    }

    @NonNull
    private int[] getMicrocodesArray(@NonNull InstructionSource source)
    {
        source.reset();
        List<Integer> m = new ArrayList<>();

        while (source.getNext())
        {
            int uCodeLength = source.getLength();

            for (int i = 0; i < uCodeLength; i++)
            {
                int data = source.getMicrocode();
                m.add(data);
            }
        }

        int[] ans = new int[m.size()];
        for (int i = 0; i < ans.length; i++)
            ans[i] = m.get(i);
        return ans;
    }
}
