package streams

class RRR extends GameDef {
  /**
   * The position where the block is located initially.
   *
   * This value is left abstract, it will be defined in concrete
   * instances of the game.
   */
  override def startPos: Pos = ???

  /**
   * The target position where the block has to go.
   * This value is left abstract.
   */
  override def goal: Pos = ???

  /**
   * The terrain of this game. This value is left abstract.
   */
  override def terrain: Terrain = ???
}
