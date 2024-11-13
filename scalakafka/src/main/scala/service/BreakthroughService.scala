package service

import model.Move
import scala.util.Random

object BreakthroughService :
  def chooseMove(legalMoves: Array[Move]): Move = {
    Random.shuffle(legalMoves).head
  }
