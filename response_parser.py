from algorithm import BIAS_SURROUNDING_CHAR


def map_marked_text_to_html(marked_text):
    paragraphs = marked_text.split("\n") # TODO check if textarea preserve linebreak!
    result = ""
    all_problems = []
    for paragraph in paragraphs:
        par_result, par_problems = map_paragraph(paragraph, len(all_problems))
        result += f"<p>{par_result}</p>"
        all_problems.extend(par_problems)

    return result, all_problems



def map_paragraph(marked_paragraph, starting_index):
    result = ""

    problems = []
    current_problem = None

    for index in range(len(marked_paragraph)):
        if marked_paragraph[index] == BIAS_SURROUNDING_CHAR:
            if current_problem is not None:
                # ending problematic comment
                result += f"<span class='highlighted' id='highlighted-{starting_index + len(problems)}'>{current_problem}</span>"
                problems.append(current_problem)
                current_problem = None
            else:
                # starting problematic comment
                current_problem = ""

        else:
            if current_problem is not None:
                current_problem += marked_paragraph[index]
            else:
                result += marked_paragraph[index]


    return result, problems

def get_comments_as_html(comments):
    result = ""
    for index, comment in enumerate(comments):
        result += f"<p id='comment-id-{index}'><strong>{comment['Text']}</strong><br/><strong>Possible bias:</strong><br/>{comment['Possible bias']}<br/><strong>Suggested improvement:</strong><br/>{comment['Suggested Improvement']}</p>"

    return result