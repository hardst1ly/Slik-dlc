package fun.slikdlc.api.storages.implement;

import fun.slikdlc.api.QClient;
import fun.slikdlc.api.storages.implement.helpertstorages.enumvar.ModuleClass;

public class ModuleStorage implements QClient {
   public ModuleStorage() {
      this.initModules();
   }

   private void initModules() {
      ModuleClass.INSTANCE.initialize();
   }
}
