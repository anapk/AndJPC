package uk.co.jads.android.jpc;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;

import org.jpc.emulator.PC;
import org.jpc.emulator.pci.peripheral.DefaultVGACard;
import org.jpc.emulator.pci.peripheral.EthernetCard;
import org.jpc.emulator.pci.peripheral.VGACard;
import org.jpc.emulator.peripheral.Keyboard;
import org.jpc.j2se.Option;
import org.jpc.j2se.VirtualClock;
import org.jpc.support.ArgProcessor;
import org.jpc.support.EthernetHub;
import org.jpc.support.EthernetOutput;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public abstract class JPCAndroidActivityHelper extends Activity implements Runnable
{
    private static final int EXIT_JPC = 5;
    private static final int LOAD_JPC = 3;
    private static final int SAVE_JPC = 4;
    private static AssetManager assets;
    private static KeyboardEmulator keyboard;
    private static PCMonitor monitor;
    @Nullable
    private static PC pc;
    private static boolean running;
    private Handler msgHandler;

    static {
        JPCAndroidActivityHelper.running = false;
    }
    
    private void loadSnapshot() throws IOException {
        this.loadSnapshot(this.openFileInput("state.dat"));
    }
    
    private void loadSnapshot(@NonNull final InputStream inputStream) throws IOException {
        final ZipInputStream zipInputStream = new ZipInputStream(inputStream);
        zipInputStream.getNextEntry();
        JPCAndroidActivityHelper.pc.loadState(zipInputStream);
        zipInputStream.closeEntry();
        ((VGACard)JPCAndroidActivityHelper.pc.getComponent(VGACard.class)).setOriginalDisplaySize();
        zipInputStream.getNextEntry();
        JPCAndroidActivityHelper.monitor.loadState(zipInputStream);
        zipInputStream.closeEntry();
        zipInputStream.close();
    }

    @NonNull
    private static InputStream openStream(final String s) throws IOException {
        Log.i("JPC", "Loading resource: " + s);
        //return JPCAndroidActivityHelper.assets.open(s);
        return JPCAndroidActivityHelper.class.getResourceAsStream("/" + s);
    }
    
    private void saveSnapshot() throws IOException {
        this.deleteFile("state.dat");
        final ZipOutputStream zipOutputStream = new ZipOutputStream(this.openFileOutput("state.dat", 0));
        zipOutputStream.putNextEntry(new ZipEntry("pc"));
        pc.saveState(zipOutputStream);
        zipOutputStream.closeEntry();
        zipOutputStream.putNextEntry(new ZipEntry("monitor"));
        monitor.saveState(zipOutputStream);
        zipOutputStream.closeEntry();
        zipOutputStream.finish();
        zipOutputStream.close();
    }
    
    private void showError(final String s, final String s2) {
        while (true) {
            try {
                Thread.sleep(500L);
                this.msgHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        final AlertDialog.Builder alertDialog$Builder = new AlertDialog.Builder(JPCAndroidActivityHelper.this);
                        alertDialog$Builder.setTitle(s);
                        alertDialog$Builder.setMessage(s2).setCancelable(false).setNegativeButton("Exit", new DialogInterface.OnClickListener() {
                            public void onClick(@NonNull final DialogInterface dialogInterface, final int n) {
                                dialogInterface.cancel();
                                JPCAndroidActivityHelper.this.forcefulExit();
                            }
                        });
                        if (JPCAndroidActivityHelper.pc != null) {
                            alertDialog$Builder.setPositiveButton("Try to continue", new DialogInterface.OnClickListener() {
                                public void onClick(@NonNull final DialogInterface dialogInterface, final int n) {
                                    dialogInterface.cancel();
                                }
                            });
                        }
                        alertDialog$Builder.show();
                    }
                });
            }
            catch (InterruptedException ex) {
                continue;
            }
            break;
        }
    }
    
    void forcefulExit() {
        if (monitor != null) {
            monitor.stopUpdateThread();
        }
        if (pc != null) {
            this.stopExecution();
        }
        System.runFinalizersOnExit(true);
        System.exit(0);
    }
    
    void init(final String[] array, @NonNull final PCMonitor monitor) {
        try {
            Option.parse(array);

            if (monitor != null) {
                monitor.stopUpdateThread();
            }
            this.monitor = monitor;
            this.msgHandler = new Handler();
            assets = this.getAssets();
            if (pc == null) {
                PC.compile = false;
                pc = new PC(new VirtualClock(), array);
            }
            EthernetOutput hub = new EthernetHub("relay.widgetry.org", 80);
            EthernetCard card = (EthernetCard) pc.getComponent(EthernetCard.class);
            card.setOutputDevice(hub);

            JPCAndroidActivityHelper.monitor.setPC(pc);
            final Keyboard keyboard = (Keyboard)pc.getComponent(Keyboard.class);
            this.keyboard = new KeyboardEmulator(keyboard, this);
            OnScreenButtons overlay = new OnScreenButtons(keyboard, this);
            MouseEmulator mouse = new MouseEmulator(keyboard, overlay);
            monitor.setScreenOverlay(overlay);
            monitor.setOnTouchListener(mouse);
            final String variable = ArgProcessor.findVariable(array, "ss", null);
            if (variable != null) {
                this.loadSnapshot(openStream(variable));
            }
            this.startExecution();
            monitor.startUpdateThread();
        }
        catch (OutOfMemoryError outOfMemoryError) {
            outOfMemoryError.printStackTrace();
            if (pc != null) pc.stop();
            pc = null;
            this.showError("Not Enough RAM", "This device does not appear to have enough RAM to start JPC. Reboot and try again.");
        }
        catch (Throwable t) {
            t.printStackTrace();
            this.showError("Execute Failed", "Sorry this device encountered a problem starting JPC: " + t.getMessage());
        }
    }
    
    public void onCreate(final Bundle bundle) {
        super.onCreate(bundle);
    }
    
    public boolean onCreateOptionsMenu(@NonNull final Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.add(0, 4, 0, "Save state").setIcon(17301582);
        menu.add(0, 3, 0, "Load state").setIcon(17301580);
        menu.add(0, 5, 0, "Screenshot").setIcon(17301560);
        //menu.add(0, 6, 0, "Screenshot");
        return true;
    }
    
    public void onDestroy() {
        JPCAndroidActivityHelper.running = false;
        super.onDestroy();
    }
    
    public boolean onKeyDown(final int n, @NonNull final KeyEvent keyEvent) {
        return keyboard.onKeyDown(n, keyEvent);
    }
    
    public boolean onKeyUp(final int n, @NonNull final KeyEvent keyEvent) {
        return keyboard.onKeyUp(n, keyEvent);
    }
    
    public boolean onOptionsItemSelected(@NonNull final MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            default: {
                return false;
            }
            case 3: {
                try {
                    monitor.stopUpdateThread();
                    stopExecution();
                    loadSnapshot();
                    startExecution();
                    monitor.startUpdateThread();
                    return true;
                }
                catch (Exception ex) {
                    this.showError("Load Failed", "Failed to load state: " + ex.getMessage());
                    return true;
                }
            }
            case 4: {
                try {
                    monitor.stopUpdateThread();
                    stopExecution();
                    saveSnapshot();
                    startExecution();
                    monitor.startUpdateThread();
                    return true;
                }
                catch (IOException ex2) {
                    showError("Save Failed", "Failed to save state: " + ex2.getMessage());
                    return true;
                }
            }
            case 5: {
                ((DefaultVGACard) pc.getComponent(VGACard.class)).saveScreenshot();
                return true;
            }
        }
    }
    
    public void run() {
        pc.start();
        try {
            while (running) {
                pc.execute();
            }
        }
        catch (OutOfMemoryError outOfMemoryError) {
            outOfMemoryError.printStackTrace();
            pc.stop();
            pc = null;
            showError("Not Enough RAM", "This device does not appear to have enough RAM to start JPC. Reboot and try again.");
        }
        catch (Throwable t) {
            t.printStackTrace();
            showError("Execute Failed", "Sorry this device encoutered a problem running JPC: " + t.getMessage());
        }
        finally {
            if (pc != null) {
                pc.stop();
            }
        }
    }
    
    void startExecution() {
        synchronized (this) {
            if (!running) {
                running = true;
                Thread runner;
                (runner = new Thread(this, "PC Execute")).start();
            }
        }
    }
    
    void stopExecution() {
        // 
        // This method could not be decompiled.
        // 
        // Original Bytecode:
        // 
        //     0: aload_0        
        //     1: monitorenter   
        //     2: iconst_0       
        //     3: putstatic       uk/co/jads/android/jpc/JPCAndroidActivityHelper.running:Z
        //     6: aload_0        
        //     7: getfield        uk/co/jads/android/jpc/JPCAndroidActivityHelper.runner:Ljava/lang/Thread;
        //    10: ifnull          56
        //    13: aload_0        
        //    14: getfield        uk/co/jads/android/jpc/JPCAndroidActivityHelper.runner:Ljava/lang/Thread;
        //    17: invokevirtual   java/lang/Thread.isAlive:()Z
        //    20: istore_2       
        //    21: iload_2        
        //    22: ifeq            56
        //    25: aload_0        
        //    26: getfield        uk/co/jads/android/jpc/JPCAndroidActivityHelper.runner:Ljava/lang/Thread;
        //    29: ldc2_w          5000
        //    32: invokevirtual   java/lang/Thread.join:(J)V
        //    35: aload_0        
        //    36: getfield        uk/co/jads/android/jpc/JPCAndroidActivityHelper.runner:Ljava/lang/Thread;
        //    39: invokevirtual   java/lang/Thread.isAlive:()Z
        //    42: istore          4
        //    44: iload           4
        //    46: ifeq            56
        //    49: aload_0        
        //    50: getfield        uk/co/jads/android/jpc/JPCAndroidActivityHelper.runner:Ljava/lang/Thread;
        //    53: invokevirtual   java/lang/Thread.stop:()V
        //    56: aload_0        
        //    57: aconst_null    
        //    58: putfield        uk/co/jads/android/jpc/JPCAndroidActivityHelper.runner:Ljava/lang/Thread;
        //    61: aload_0        
        //    62: monitorexit    
        //    63: return         
        //    64: astore_1       
        //    65: aload_0        
        //    66: monitorexit    
        //    67: aload_1        
        //    68: athrow         
        //    69: astore          5
        //    71: goto            56
        //    74: astore_3       
        //    75: goto            35
        //    Exceptions:
        //  Try           Handler
        //  Start  End    Start  End    Type                            
        //  -----  -----  -----  -----  --------------------------------
        //  2      21     64     69     Any
        //  25     35     74     78     Ljava/lang/InterruptedException;
        //  25     35     64     69     Any
        //  35     44     64     69     Any
        //  49     56     69     74     Ljava/lang/SecurityException;
        //  49     56     64     69     Any
        //  56     61     64     69     Any
        // 
        // The error that occurred was:
        // 
        // java.lang.IllegalStateException: Expression is linked from several locations: Label_0035:
        //     at com.strobel.decompiler.ast.Error.expressionLinkedFromMultipleLocations(Error.java:27)
        //     at com.strobel.decompiler.ast.AstOptimizer.mergeDisparateObjectInitializations(AstOptimizer.java:2592)
        //     at com.strobel.decompiler.ast.AstOptimizer.optimize(AstOptimizer.java:235)
        //     at com.strobel.decompiler.ast.AstOptimizer.optimize(AstOptimizer.java:42)
        //     at com.strobel.decompiler.languages.java.ast.AstMethodBodyBuilder.createMethodBody(AstMethodBodyBuilder.java:214)
        //     at com.strobel.decompiler.languages.java.ast.AstMethodBodyBuilder.createMethodBody(AstMethodBodyBuilder.java:99)
        //     at com.strobel.decompiler.languages.java.ast.AstBuilder.createMethodBody(AstBuilder.java:757)
        //     at com.strobel.decompiler.languages.java.ast.AstBuilder.createMethod(AstBuilder.java:655)
        //     at com.strobel.decompiler.languages.java.ast.AstBuilder.addTypeMembers(AstBuilder.java:532)
        //     at com.strobel.decompiler.languages.java.ast.AstBuilder.createTypeCore(AstBuilder.java:499)
        //     at com.strobel.decompiler.languages.java.ast.AstBuilder.createTypeNoCache(AstBuilder.java:141)
        //     at com.strobel.decompiler.languages.java.ast.AstBuilder.createType(AstBuilder.java:130)
        //     at com.strobel.decompiler.languages.java.ast.AstBuilder.addType(AstBuilder.java:105)
        //     at com.strobel.decompiler.languages.java.JavaLanguage.buildAst(JavaLanguage.java:71)
        //     at com.strobel.decompiler.languages.java.JavaLanguage.decompileType(JavaLanguage.java:59)
        //     at com.strobel.decompiler.DecompilerDriver.decompileType(DecompilerDriver.java:304)
        //     at com.strobel.decompiler.DecompilerDriver.decompileJar(DecompilerDriver.java:225)
        //     at com.strobel.decompiler.DecompilerDriver.main(DecompilerDriver.java:125)
        // 
        throw new IllegalStateException("An error occurred while decompiling this method.");
    }
}
