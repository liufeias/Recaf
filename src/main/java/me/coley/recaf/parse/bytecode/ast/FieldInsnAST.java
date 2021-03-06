package me.coley.recaf.parse.bytecode.ast;

import me.coley.recaf.parse.bytecode.Variables;
import org.objectweb.asm.tree.*;

import java.util.Map;

/**
 * Field reference instruction AST.
 *
 * @author Matt
 */
public class FieldInsnAST extends InsnAST {
	private final TypeAST owner;
	private final NameAST name;
	private final DescAST desc;

	/**
	 * @param line
	 * 		Line number this node is written on.
	 * @param start
	 * 		Offset from line start this node starts at.
	 * @param opcode
	 * 		Opcode AST.
	 * @param owner
	 * 		Field owner type AST.
	 * @param name
	 * 		Field name AST.
	 * @param desc
	 * 		Field descriptor AST.
	 */
	public FieldInsnAST(int line, int start, OpcodeAST opcode, TypeAST owner, NameAST name, DescAST desc) {
		super(line, start, opcode);
		this.owner = owner;
		this.name = name;
		this.desc = desc;
		addChild(owner);
		addChild(name);
		addChild(desc);
	}

	/**
	 * @return Type AST of field owner.
	 */
	public TypeAST getOwner() {
		return owner;
	}

	/**
	 * @return Name AST of field name.
	 */
	public NameAST getName() {
		return name;
	}

	/**
	 * @return Desc AST of field descriptor.
	 */
	public DescAST getDesc() {
		return desc;
	}

	@Override
	public String print() {
		return getOpcode().print() + " " + owner.print() + "." + name.print() + " " + desc.print();
	}

	@Override
	public AbstractInsnNode compile(Map<String, LabelNode> labels, Variables variables) {
		return new FieldInsnNode(getOpcode().getOpcode(), getOwner().getType(),
				getName().getName(), getDesc().getDesc());
	}
}
