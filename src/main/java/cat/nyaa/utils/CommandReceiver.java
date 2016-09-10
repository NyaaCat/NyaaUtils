package cat.nyaa.utils;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

public abstract class CommandReceiver<T extends JavaPlugin> implements CommandExecutor {
    //==== Error Definitions ====//
    private static class NotPlayerException extends RuntimeException {
    }

    private static class NoItemInHandException extends RuntimeException {
        public final boolean isOffHand;

        public NoItemInHandException(boolean ifh) {
            isOffHand = ifh;
        }
    }

    private static class NoPermissionException extends RuntimeException {
        public NoPermissionException(String permission) {
            super(permission);
        }
    }

    private static class BadCommandException extends RuntimeException {
        public final Object[] objs;

        public BadCommandException(String msg_internal, Object... args) {
            super(msg_internal);
            objs = args;
        }

        public BadCommandException(String msg_internal, Throwable cause, Object... args) {
            super(msg_internal, cause);
            objs = args;
        }
    }

    private final Internationalization i18n;
    // final T plugin;
    private final Map<String, Method> subCommands = new HashMap<>();
    private final Map<String, CommandReceiver> subCommandClasses = new HashMap<>();
    private final Map<String, String> subCommandPermission = new HashMap<>();

    private Set<Method> getAllMethods(Class cls) {
        Set<Method> ret = new HashSet<>();
        while (cls != null) {
            ret.addAll(Arrays.asList(cls.getDeclaredMethods()));
            cls = cls.getSuperclass();
        }
        return ret;
    }

    private Set<Field> getAllFields(Class cls) {
        Set<Field> ret = new HashSet<>();
        while (cls != null) {
            ret.addAll(Arrays.asList(cls.getDeclaredFields()));
            cls = cls.getSuperclass();
        }
        return ret;
    }

    public CommandReceiver(T plugin, Internationalization i18n) {
        //this.plugin = plugin;
        this.i18n = i18n;

        for (Method m : getAllMethods(getClass())) {
            SubCommand anno = m.getAnnotation(SubCommand.class);
            if (anno == null) continue;
            Class<?>[] params = m.getParameterTypes();
            if (!(params.length == 2 &&
                    params[0] == CommandSender.class &&
                    params[1] == Arguments.class)) {
                plugin.getLogger().warning(i18n.get("internal.warn.bad_subcommand", m.toString()));
            } else {
                m.setAccessible(true);
                subCommands.put(anno.value().toLowerCase(), m);
                if (!anno.permission().equals(""))
                    subCommandPermission.put(anno.value(), anno.permission());
            }
        }

        for (Field f : getAllFields(getClass())) {
            SubCommand anno = f.getAnnotation(SubCommand.class);
            if (anno == null) continue;
            if (CommandReceiver.class.isAssignableFrom(f.getType())) {
                CommandReceiver<T> obj = null;
                try {
                    Class<? extends CommandReceiver<T>> cls = (Class<? extends CommandReceiver<T>>) f.getType();
                    Constructor<? extends CommandReceiver<T>> con = cls.getDeclaredConstructor(Object.class, Internationalization.class);
                    obj = con.newInstance(plugin, i18n);
                    if (obj != null) {
                        subCommandClasses.put(anno.value().toLowerCase(), obj);
                        f.setAccessible(true);
                        f.set(this, obj);
                    }
                } catch (ReflectiveOperationException ex) {
                    plugin.getLogger().warning(i18n.get("internal.warn.bad_subcommand", f.toString()));
                    obj = null;
                    ex.printStackTrace();
                }
            } else {
                plugin.getLogger().warning(i18n.get("internal.warn.bad_subcommand", f.toString()));
            }
        }
    }

    public List<String> getSubcommands() {
        ArrayList<String> ret = new ArrayList<>();
        ret.addAll(subCommands.keySet());
        ret.addAll(subCommandClasses.keySet());
        ret.sort(String::compareTo);
        return ret;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Arguments cmd = Arguments.parse(args);
        if (cmd == null) return false;
        acceptCommand(sender, cmd);
        return true;
    }

    public void acceptCommand(CommandSender sender, Arguments cmd) {
        try {
            String subCommand = cmd.next();
            if (subCommand == null) subCommand = "help";
            boolean hasCommand = subCommands.containsKey(subCommand.toLowerCase()) || subCommandClasses.containsKey(subCommand.toLowerCase());
            boolean subClassCommand = subCommandClasses.containsKey(subCommand.toLowerCase());
            if (cmd.length() == 0 || !hasCommand) {
                subCommand = "help";
            }

            if (subCommandPermission.containsKey(subCommand)) {
                if (!sender.hasPermission(subCommandPermission.get(subCommand))) {
                    throw new NoPermissionException(subCommandPermission.get(subCommand));
                }
            }

            try {
                if (subClassCommand) {
                    subCommandClasses.get(subCommand.toLowerCase()).acceptCommand(sender, cmd);
                } else {
                    subCommands.get(subCommand.toLowerCase()).invoke(this, sender, cmd);
                }
            } catch (ReflectiveOperationException ex) {
                Throwable cause = ex.getCause();
                if (cause != null && cause instanceof RuntimeException)
                    throw (RuntimeException) cause;
                else
                    throw new RuntimeException("Failed to invoke subcommand", ex);
            }
            if (!subClassCommand) msg(sender, "user.info.command_complete");
        } catch (NotPlayerException ex) {
            msg(sender, "user.info.not_player");
        } catch (NoItemInHandException ex) {
            msg(sender, ex.isOffHand ? "user.info.no_item_offhand" : "user.info.no_item_hand");
        } catch (BadCommandException ex) {
            sender.sendMessage(i18n.get(ex.getMessage(), ex.objs));
        } catch (NoPermissionException ex) {
            msg(sender, "user.error.no_required_permission", ex.getMessage());
        } catch (Exception ex) {
            ex.printStackTrace();
            msg(sender, "user.error.command_exception");
        }
    }

    public abstract String getHelpPrefix();

    private String getHelpContent(String type, String... subkeys) {
        String key = "manual";
        for (String s : subkeys) {
            if (s.length() > 0)
                key += "." + s;
        }
        key += "." + type;
        if (i18n.hasKey(key)) {
            return i18n.get(key);
        } else {
            return i18n.get("manual.no_" + type);
        }
    }

    @SubCommand("help")
    public void printHelp(CommandSender sender, Arguments args) {
        List<String> cmds = getSubcommands();
        String tmp = "";
        for (String cmd : cmds) {
            tmp += "\n    " + cmd + ":  " + getHelpContent("description", getHelpPrefix(), cmd);
            tmp += "\n    " + cmd + ":  " + getHelpContent("usage", getHelpPrefix(), cmd);
        }
        sender.sendMessage(tmp);
    }

    public static Player asPlayer(CommandSender target) {
        if (target instanceof Player) {
            return (Player) target;
        } else {
            throw new NotPlayerException();
        }
    }

    public void msg(CommandSender target, String template, Object... args) {
        target.sendMessage(i18n.get(template, args));
    }

    public static ItemStack getItemInHand(CommandSender se) {
        if (se instanceof Player) {
            Player p = (Player) se;
            if (p.getInventory() != null) {
                ItemStack i = p.getInventory().getItemInMainHand();
                if (i != null && i.getType() != Material.AIR) {
                    return i;
                }
            }
            throw new NoItemInHandException(false);
        } else {
            throw new NotPlayerException();
        }
    }

    public static ItemStack getItemInOffHand(CommandSender se) {
        if (se instanceof Player) {
            Player p = (Player) se;
            if (p.getInventory() != null) {
                ItemStack i = p.getInventory().getItemInOffHand();
                if (i != null && i.getType() != Material.AIR) {
                    return i;
                }
            }
            throw new NoItemInHandException(true);
        } else {
            throw new NotPlayerException();
        }
    }

    public static class Arguments {

        private List<String> parsedArguments = new ArrayList<>();
        private int index = 0;

        private Arguments() {
        }

        public static Arguments parse(String[] rawArg) {
            if (rawArg.length == 0) return new Arguments();
            String cmd = rawArg[0];
            for (int i = 1; i < rawArg.length; i++)
                cmd += " " + rawArg[i];

            List<String> cmdList = new ArrayList<>();
            boolean escape = false, quote = false;
            String tmp = "";
            for (int i = 0; i < cmd.length(); i++) {
                char chr = cmd.charAt(i);
                if (escape) {
                    if (chr == '\\' || chr == '`') tmp += chr;
                    else return null; // bad escape char
                    escape = false;
                } else if (chr == '\\') {
                    escape = true;
                } else if (chr == '`') {
                    if (quote) {
                        if (i + 1 == cmd.length() || cmd.charAt(i + 1) == ' ') {
                            cmdList.add(tmp);
                            tmp = "";
                            i++;
                            quote = false;
                        } else {
                            return null; //bad quote end
                        }
                    } else {
                        if (tmp.length() > 0)
                            return null; // bad quote start
                        quote = true;
                    }
                } else if (chr == ' ') {
                    if (quote) {
                        tmp += ' ';
                    } else if (tmp.length() > 0) {
                        cmdList.add(tmp);
                        tmp = "";
                    }
                } else {
                    tmp += chr;
                }
            }
            if (tmp.length() > 0) cmdList.add(tmp);
            if (escape || quote) return null;

            Arguments ret = new Arguments();
            ret.parsedArguments = cmdList;
            return ret;
        }

        public String at(int index) {
            return parsedArguments.get(index);
        }

        public String next() {
            if (index < parsedArguments.size())
                return parsedArguments.get(index++);
            else
                return null;
        }

        public String top() {
            if (index < parsedArguments.size())
                return parsedArguments.get(index);
            else
                return null;
        }

        public int nextInt() {
            String str = next();
            if (str == null) throw new BadCommandException("No more integers in argument");
            if (str.endsWith("k")) str = str.substring(0, str.length() - 1) + "000";
            try {
                return Integer.parseInt(str);
            } catch (NumberFormatException ex) {
                throw new BadCommandException("user.error.not_int", ex, str);
            }
        }

        public double nextDouble() {
            String str = next();
            if (str == null) throw new BadCommandException("No more numbers in argument");
            try {
                return Double.parseDouble(str);
            } catch (NumberFormatException ex) {
                throw new BadCommandException("user.error.not_double", ex, str);
            }
        }

        public <T extends Enum<T>> T nextEnum(Class<T> cls) {
            String str = next();
            if (str == null) throw new BadCommandException("No more EnumValues in argument");
            try {
                return Enum.valueOf(cls, str);
            } catch (IllegalArgumentException ex) {
                String vals = "";
                List<String> l = new ArrayList<>();
                for (T k : cls.getEnumConstants()) {
                    l.add(k.name());
                }
                l.sort(Comparator.naturalOrder());
                for (String k : l) vals += "\n" + k;

                throw new BadCommandException("user.error.bad_enum", cls.getName(), vals);
            }
        }

        public boolean nextBoolean() {
            String str = next();
            if (str == null) throw new BadCommandException("No more booleans in argument");
            return Boolean.parseBoolean(str);
        }

        public int length() {
            return parsedArguments.size();
        }
    }

    @Target({ElementType.METHOD, ElementType.FIELD})
    @Retention(RetentionPolicy.RUNTIME)
    public @interface SubCommand {
        String value();

        String permission() default "";
    }
}
