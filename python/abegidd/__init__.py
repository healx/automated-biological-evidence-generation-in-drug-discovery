def first(iterable):
    iterator = iter(iterable)

    try:
        return next(iterator)
    except StopIteration:
        raise ValueError("Iterable was empty")


def flatten(iterables):
    result = []

    for iterable in iterables:
        for element in iterable:
            result.append(element)

    return result
