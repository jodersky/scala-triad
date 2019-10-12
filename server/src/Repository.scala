package triad

import java.time.Instant

import slick.jdbc.{JdbcProfile, SQLiteProfile}

/** Slick wrapper around the persisted message database. */
class Repository(val profile: JdbcProfile, url: String, driver: String) {
  val database: profile.backend.DatabaseDef =
    profile.api.Database.forURL(url, driver)

  import profile.api._

  implicit val instantColumnType = MappedColumnType.base[Instant, Long](
    { i =>
      i.toEpochMilli()
    }, { l =>
      Instant.ofEpochMilli(l)
    }
  )

  class Messages(tag: Tag) extends Table[Message](tag, "messages") {
    def id = column[String]("id")
    def content = column[String]("content")
    def author = column[String]("author")
    def timestamp = column[Instant]("timestamp")
    def * =
      (id, content, author, timestamp) <> ({ cols =>
        Message(cols._2, cols._3, cols._4)
      }, { message: Message =>
        Some((message.id, message.content, message.author, message.timestamp))
      })
    def pk = primaryKey("pk", id)
  }

  val Messages = TableQuery[Messages]

  def initAction = DBIO.seq(
    Messages.schema.create,
    Messages += Message("first!", "John Smith")
  )

}

object Repository {

  def sqlite(name: String) =
    new Repository(SQLiteProfile, s"jdbc:sqlite:$name", "org.sqlite.JDBC")

}
