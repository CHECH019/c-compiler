#include <iostream>
using namespace std;

int main() {
    int num1 = 10;
    int num2 = 20;
    int sum = num1 + num2;

    cout << "The sum is: " << sum << endl;

    if (sum > 15) {
        cout << "The sum is greater than 15" << endl;
    } else {
        cout << "The sum is not greater than 15" << endl;
    }

    return 0;
}