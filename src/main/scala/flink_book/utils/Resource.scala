package flink_book.utils

object Resource {
  def using[T, R <: AutoCloseable](resource: R)(block: R => T): T = {
    try {
      block(resource)
    } finally {
      resource.close()
    }
  }
}