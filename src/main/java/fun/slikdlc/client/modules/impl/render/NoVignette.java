package fun.slikdlc.client.modules.impl.render;

import fun.slikdlc.client.modules.Module;

public class NoVignette extends Module {
   public static NoVignette INSTANCE = new NoVignette();

   public NoVignette() {
      super("NoVignette", "Убирает затемнения на краях экрана", Module.ModuleCategory.RENDER);
   }
}
