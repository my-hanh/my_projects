import numpy as np
import matplotlib.pyplot as plt
import pickle


class Data:
    def __init__(self, feature, label):
        self.feature = feature
        self.label = label


def prepare_data(data):
    labels = np.unique(np.array([dat.label for dat in data]))
    class_dim = len(labels)
    features = [[] for i in range(class_dim)]
    for dat in data:
        features[dat.label].append(dat.feature)
    return labels, features


def train(train_data):
    # Return
    # mean ... list with one entry per class
    #          each entry is the mean of the feature vectors of a class
    # covariance ... list with one entry per class
    #          each entry is the covariance of the feature vectors of a class
    labels, features = prepare_data(train_data)

    means = []
    covariances = []
    for label in labels:
        class_features = features[label]
        N = len(class_features)
        d = len(class_features[0])

        mean_list = [0.0] * d
        for x in class_features:
            for k in range(d):
                mean_list[k] += float(x[k])

        for k in range(d):
            mean_list[k] /= N

        m = np.array(mean_list)
        means.append(m)

        # C[a,b] = 1/(N-1) * sum_i (x_i[a]-m[a])*(x_i[b]-m[b])
        C = np.zeros((d, d))
        for x in class_features:
            for a in range(d):
                da = float(x[a]) - mean_list[a]
                for b in range(d):
                    db = float(x[b]) - mean_list[b]
                    C[a, b] += da * db

        if N > 1:
            for a in range(d):
                for b in range(d):
                    C[a, b] /= (N - 1)

        covariances.append(C)

    return means, covariances


def evaluateCost(feature_vector, m, c):
    # Input
    # feature_vector ... feature vector under test
    # m     mean of the feature vectors for a class
    # c     covariance of the feature vectors of a class
    # Output
    #   some scalar proportional to the logarithm fo the probability d_j(feature_vector)
    x = np.asarray(feature_vector)
    mean_list = np.asarray(m)
    covariance = np.asarray(c)

    diff = x - mean_list

    det = np.linalg.det(covariance)

    # Mahalanobis term: (x - m)^T C^{-1} (x - m)
    C_inv = np.linalg.inv(covariance)
    mahal = diff.T @ C_inv @ diff

    # cost (same form you tried, but correct)
    cost = -0.5 * np.log(det) - 0.5 * mahal

    return cost

def classify(test_data, mean, covariance):

    n_classes = len(mean)
    decisions = []

    for sample in test_data:
        x = sample.feature

        best_class = 0
        best_score = evaluateCost(x, mean[0], covariance[0])

        # check every class j
        for j in range(1, n_classes):
            score = evaluateCost(x, mean[j], covariance[j])
            if score > best_score:  # choose class with highest score
                best_score = score
                best_class = j

        decisions.append(best_class)

    return decisions



def computeConfusionMatrix(decisions, test_data):
    # confusion_matrix[true_label, predicted_label] = count
    true_labels = [dat.label for dat in test_data]
    n_classes = max(max(true_labels), max(decisions)) + 1

    cm = np.zeros((n_classes, n_classes), dtype=int)
    for t, p in zip(true_labels, decisions):
        cm[t, p] += 1

    return cm

def plot_data_and_classifier(train_data, means, covariances):
    # ---- extract training data for plotting ----
    xs = np.array([d.feature for d in train_data])
    labels = np.array([d.label for d in train_data])

    # We can only visualize in 2D → use feature[0] and feature[1]
    x0 = xs[:, 0]
    x1 = xs[:, 1]

    # ---- Plot training data ----
    plt.figure(figsize=(10, 5))

    plt.subplot(1, 2, 1)
    for lbl in np.unique(labels):
        pts = xs[labels == lbl]
        plt.scatter(pts[:, 0], pts[:, 1], label=f"Class {lbl}", s=15)
    plt.title("Training data (using features 0 and 1)")
    plt.xlabel("Feature 0")
    plt.ylabel("Feature 1")
    plt.legend()

    # ---- Classifier grid ----
    # create a grid of points in the feature space
    x_min, x_max = x0.min() - 1, x0.max() + 1
    y_min, y_max = x1.min() - 1, x1.max() + 1

    xx, yy = np.meshgrid(
        np.linspace(x_min, x_max, 200),
        np.linspace(y_min, y_max, 200)
    )

    # classify each point in the grid
    grid_points = np.c_[xx.ravel(), yy.ravel()]
    # pad with zeros for missing features 2,3
    grid_points_4D = np.hstack([grid_points, np.zeros((grid_points.shape[0], 2))])

    Z = []
    for point in grid_points_4D:
        scores = [evaluateCost(point, means[j], covariances[j])
                  for j in range(len(means))]
        Z.append(np.argmax(scores))
    Z = np.array(Z).reshape(xx.shape)

    # ---- Plot decision regions ----
    plt.subplot(1, 2, 2)
    plt.contourf(xx, yy, Z, alpha=0.4, levels=len(means))
    for lbl in np.unique(labels):
        pts = xs[labels == lbl]
        plt.scatter(pts[:, 0], pts[:, 1], s=15)
    plt.title("Classifier decision regions")
    plt.xlabel("Feature 0")
    plt.ylabel("Feature 1")

    plt.tight_layout()
    plt.show()



def main():
    train_data = pickle.load(open("train_data.pkl", "rb"))
    test_data = pickle.load(open("test_data.pkl", "rb"))

    # Train: Compute mean and covariance for each object class from {0,1,2,3}
    # returns one list entry per object class
    mean, covariance = train(train_data)
    print(f"means: {mean}")
    print(f"covariance: {covariance}")
    
    # Decide: Compute decision for each feature vector from test_data
    # return a list of class indices from the set {0,1,2,3}
    decisions = classify(test_data, mean, covariance)
    print("decisions: ")
    print(decisions)
    
    # Compute the confusion matrix
    confusion_matrix = computeConfusionMatrix(decisions, test_data)
    print("Confusion Matrix: ")
    print(confusion_matrix)

    # Plot
    plot_data_and_classifier(train_data, mean, covariance)

if __name__ == "__main__":
    main()
