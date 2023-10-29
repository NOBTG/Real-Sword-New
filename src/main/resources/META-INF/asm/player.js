var ASM = Java.type('net.minecraftforge.coremod.api.ASMAPI');
var Opcodes = Java.type('org.objectweb.asm.Opcodes');

var MethodInsnNode = Java.type('org.objectweb.asm.tree.MethodInsnNode');
var VarInsnNode = Java.type('org.objectweb.asm.tree.VarInsnNode');
var InsnList = Java.type('org.objectweb.asm.tree.InsnList');

function initializeCoreMod() {
    return {
        'player': {
            'target': {
                'type': 'METHOD',
                'class': 'net.minecraft.world.entity.player.Player',
                'methodName': ASM.mapMethod('m_8119_'),
                'methodDesc': '()V'
            }, 'transformer': function (methodNode) {
                var instructions = methodNode.instructions;
                var newInstructions = new InsnList();
                newInstructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
                newInstructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC, 'com/nobtg/Utils/EntityUtil', 'getRealSword', '(Lnet/minecraft/world/entity/player/Player;)V', false));
                newInstructions.add(instructions);
                instructions.add(newInstructions);
                return methodNode;
            }
        }
    };
}