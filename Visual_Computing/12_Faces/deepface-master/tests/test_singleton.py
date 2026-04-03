from face_example1.commons.logger import Logger

logger = Logger()


def test_singleton_same_object():
    assert Logger() == Logger()
    logger.info("✅ id's of instances of \"singletoned\" class Logger are the same")
