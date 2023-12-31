var ASM = Java.type('net.minecraftforge.coremod.api.ASMAPI');
var Opcodes = Java.type('org.objectweb.asm.Opcodes');

var MethodInsnNode = Java.type('org.objectweb.asm.tree.MethodInsnNode');
var InsnList = Java.type('org.objectweb.asm.tree.InsnList');

function initializeCoreMod() {
    return {
        'font_tick': {
            'target': {
                'type': 'METHOD',
                'class': 'net.minecraft.client.Minecraft',
                'methodName': ASM.mapMethod('m_91398_'),
                'methodDesc': '()V'
            }, 'transformer': function (methodNode) {
                var instructions = methodNode.instructions;
                var newInstructions = new InsnList();
                newInstructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC, 'github/nobtg/Utils/ClientUtil', 'font_tick', '()V', false));
                newInstructions.add(instructions);
                instructions.add(newInstructions);
                return methodNode;
            }
        }
    };
}