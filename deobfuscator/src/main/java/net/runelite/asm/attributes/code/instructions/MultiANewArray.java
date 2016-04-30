package net.runelite.asm.attributes.code.instructions;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import net.runelite.asm.ClassFile;
import net.runelite.asm.ClassGroup;
import net.runelite.asm.attributes.code.Instruction;
import net.runelite.asm.attributes.code.InstructionType;
import net.runelite.asm.attributes.code.Instructions;
import net.runelite.asm.execution.Frame;
import net.runelite.asm.execution.InstructionContext;
import net.runelite.asm.execution.Stack;
import net.runelite.asm.execution.StackContext;
import net.runelite.asm.execution.Type;
import net.runelite.asm.execution.Value;
import net.runelite.asm.pool.Class;

public class MultiANewArray extends Instruction
{
	private Class clazz;
	private int dimensions;
	private ClassFile myClass;

	public MultiANewArray(Instructions instructions, InstructionType type, int pc)
	{
		super(instructions, type, pc);
	}
	
	@Override
	public void load(DataInputStream is) throws IOException
	{
		clazz = this.getPool().getClass(is.readUnsignedShort());
		dimensions = is.readUnsignedByte();
		length += 3;
	}
	
	@Override
	public void write(DataOutputStream out) throws IOException
	{
		super.write(out);
		out.writeShort(this.getPool().make(clazz));
		out.writeByte(dimensions);
	}
	
	@Override
	public InstructionContext execute(Frame frame)
	{
		InstructionContext ins = new InstructionContext(this, frame);
		Stack stack = frame.getStack();
		
		Value[] lenghts = new Value[dimensions];
		for (int i = 0; i < dimensions; ++i)
		{
			StackContext ctx = stack.pop();
			ins.pop(ctx);
			
			lenghts[i] = ctx.getValue();
		}
		
		Type t = new Type(new net.runelite.asm.signature.Type(clazz.getName()));
		StackContext ctx = new StackContext(ins, t, Value.newArray(lenghts));
		stack.push(ctx);
		
		ins.push(ctx);
		
		return ins;
	}
	
	@Override
	public void lookup()
	{
		net.runelite.asm.signature.Type t = new net.runelite.asm.signature.Type(clazz.getName());
		String name = t.getType();
		if (name.startsWith("L") && name.endsWith(";"))
			name = name.substring(1, name.length() - 1);
		ClassGroup group = this.getInstructions().getCode().getAttributes().getClassFile().getGroup();
		myClass = group.findClass(name);
	}
	
	@Override
	public void regeneratePool()
	{
		if (myClass != null)
		{
			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < this.dimensions; ++i)
				sb.append('[');
			if (this.dimensions > 0)
				sb.append("L" + myClass.getName() + ";");
			else
				sb.append(myClass.getName());
			clazz = new Class(sb.toString());
		}
	}
}