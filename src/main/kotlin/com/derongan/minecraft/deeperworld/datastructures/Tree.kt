package com.derongan.minecraft.deeperworld.datastructures

import org.bukkit.entity.Entity

//TODO: Might want to move to IdoFront
/**
 * A generic tree structure implementation.
 */
class TreeNode<T>(val value: T) {
    var parent: TreeNode<T>? = null
    var children: MutableList<TreeNode<T>> = mutableListOf()

    /**
     * Add a node as the child of this node.
     */
    fun addChild(node: TreeNode<T>) {
        children.add(node)
        node.parent = this
    }

    /**
     * Apply the given function to each node in the tree.
     */
    fun applyAll(applyFun: (TreeNode<T>) -> Unit) {
        applyFun(this)
        children.forEach { child -> child.applyAll(applyFun) }
    }

    /**
     * Get a list of nodes in the tree that satisfy the given condition.
     */
    fun where(condition: (TreeNode<T>) -> Boolean): List<TreeNode<T>> {
        mutableListOf<TreeNode<T>>().let { returnList ->
            if (condition(this)) {
                returnList.add(this)
            }

            children.forEach { returnList.addAll(it.where(condition)) }

            return returnList
        }
    }

    /**
     * Get the values of all the nodes in the tree
     */
    fun values(): List<T> {
        mutableListOf<T>().let { returnList ->
            returnList.add(value)

            children.forEach { returnList.addAll(it.values()) }

            return returnList
        }
    }
}

class VehicleTree(rootVehicle: Entity) {
    val root = TreeNode<Entity>(rootVehicle)

    init {
        addPassengerChildren(root)
    }

    private fun addPassengerChildren(node: TreeNode<Entity>) {
        node.value.passengers.forEach { passenger ->
            TreeNode<Entity>(passenger).let {
                node.addChild(it)
                addPassengerChildren(it)
            }
        }
    }
}
